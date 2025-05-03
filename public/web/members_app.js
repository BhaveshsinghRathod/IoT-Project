/**
 * The App class is a controller holding the global state.
 * It creates all children controllers in render().
 */
class MembersApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            members: new MembersData([], []),
            newGroupName: ""
        };
    }

    componentDidMount() {
        // Fetch initial data and set up periodic refresh (for real-time sync)
        this.fetchData();
        this.intervalId = window.setInterval(() => this.fetchData(), 1000);
    }

    componentWillUnmount() {
        if (this.intervalId) {
            window.clearInterval(this.intervalId);
        }
    }

    fetchData() {
        Promise.all([fetch("../api/groups"), fetch("../api/plugs")])
            .then(responses => Promise.all(responses.map(r => r.json())))
            .then(([groupsData, plugsData]) => {
                const updatedData = new MembersData(groupsData, plugsData);
                this.setState({ members: updatedData });
            })
            .catch(err => console.debug("MembersApp: error " + JSON.stringify(err)));
    }

    addGroup = () => {
        const groupName = this.state.newGroupName.trim();
        if (groupName === "") return;
        fetch("../api/groups/" + encodeURIComponent(groupName), {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify([])  // create an empty group
        })
        .then(() => {
            // Clear input and refresh data
            this.setState({ newGroupName: "" });
            this.fetchData();
        })
        .catch(err => console.debug("MembersApp: addGroup error " + JSON.stringify(err)));
    }

    removeGroup = (groupName) => {
        fetch("../api/groups/" + encodeURIComponent(groupName), {
            method: "DELETE"
        })
        .then(() => {
            this.fetchData();
        })
        .catch(err => console.debug("MembersApp: removeGroup error " + JSON.stringify(err)));
    }

    toggleGroup = (groupName) => {
        fetch("../api/groups/" + encodeURIComponent(groupName) + "?action=toggle")
        .then(() => {
            // After toggling, updated plug states will be fetched in the next poll cycle
        })
        .catch(err => console.debug("MembersApp: toggleGroup error " + JSON.stringify(err)));
    }

    toggleMember = (plugName, groupName, isMember) => {
        // Update membership for one plug in one group
        const membersData = this.state.members;
        let currentMembers = membersData.membership[groupName] ? new Set(membersData.membership[groupName]) : new Set();
        if (isMember) {
            currentMembers.add(plugName);    // adding plug to group
        } else {
            currentMembers.delete(plugName); // removing plug from group
        }
        const newMembersList = Array.from(currentMembers);
        fetch("../api/groups/" + encodeURIComponent(groupName), {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(newMembersList)
        })
        .catch(err => console.debug("MembersApp: toggleMember error " + JSON.stringify(err)));
        // Optimistically update local state for immediate UI feedback:
        const updatedMembership = { ...membersData.membership, [groupName]: new Set(currentMembers) };
        const updatedData = new MembersData(membersData.groupList, membersData.plugList, membersData.plugStates, updatedMembership);
        this.setState({ members: updatedData });
    }

    removeMemberFromAll = (plugName) => {
        const membersData = this.state.members;
        // Find all groups that currently include this plug
        const groupsToUpdate = membersData.get_group_names().filter(groupName =>
            membersData.is_member_in_group(plugName, groupName)
        );
        // Remove plug from each of those groups
        groupsToUpdate.forEach(groupName => {
            let newMembers = new Set(membersData.membership[groupName]);
            newMembers.delete(plugName);
            fetch("../api/groups/" + encodeURIComponent(groupName), {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(Array.from(newMembers))
            })
            .catch(err => console.debug("MembersApp: removeMemberFromAll error " + JSON.stringify(err)));
        });
        // Update local state immediately
        const newMembership = {};
        for (let groupName of membersData.get_group_names()) {
            newMembership[groupName] = new Set(membersData.membership[groupName]);
            newMembership[groupName].delete(plugName);
        }
        const updatedData = new MembersData(membersData.groupList, membersData.plugList, membersData.plugStates, newMembership);
        this.setState({ members: updatedData });
    }

    handleNameInputChange = (event) => {
        this.setState({ newGroupName: event.target.value });
    }

    render() {
        return (
            <div className="container">
                <div className="row">
                    <div className="col-sm-12">
                        <h2>Manage Groups and Members</h2>
                        {/* Group creation form */}
                        <div className="form-inline mb-2">
                            <input type="text" className="form-control mr-2" placeholder="New Group Name"
                                   value={this.state.newGroupName}
                                   onChange={this.handleNameInputChange} />
                            <button className="btn btn-primary" onClick={this.addGroup}>Add Group</button>
                        </div>
                        {/* Groups & Members table */}
                        <MembersTable
                            members={this.state.members}
                            toggleGroup={this.toggleGroup}
                            removeGroup={this.removeGroup}
                            toggleMember={this.toggleMember}
                            removeMemberFromAll={this.removeMemberFromAll}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

class MembersData {
    constructor(groupsData = [], plugsData = [], plugStates = null, membershipMap = null) {
        this.membership = {};
        this.groupList = [];
        this.plugList = [];
        this.plugStates = {};
        if (Array.isArray(groupsData)) {
            // Initialize from fetched group data
            for (let group of groupsData) {
                if (!group.name) continue;
                this.groupList.push(group.name);
                this.membership[group.name] = new Set();
                if (Array.isArray(group.members)) {
                    for (let plug of group.members) {
                        this.membership[group.name].add(plug.name);
                    }
                }
            }
        } else {
            // Clone from existing data (when doing local updates)
            this.groupList = Array.isArray(groupsData) ? groupsData.slice() : [];
            for (let g of this.groupList) {
                this.membership[g] = membershipMap && membershipMap[g] ? new Set(membershipMap[g]) : new Set();
            }
        }
        if (Array.isArray(plugsData)) {
            for (let plug of plugsData) {
                this.plugList.push(plug.name);
                this.plugStates[plug.name] = plug.state;
            }
        } else {
            // Clone plug list and states from existing data
            this.plugList = Array.isArray(plugsData) ? plugsData.slice() : [];
            if (plugStates) {
                this.plugStates = { ...plugStates };
            } else {
                for (let name of this.plugList) {
                    this.plugStates[name] = "off";
                }
            }
        }
        // Sort lists for consistent order
        this.groupList.sort();
        this.plugList.sort();
    }
    get_group_names() {
        return this.groupList;
    }
    get_member_names() {
        return this.plugList;
    }
    is_member_in_group(memberName, groupName) {
        return this.membership[groupName] ? this.membership[groupName].has(memberName) : false;
    }
}

// export
window.MembersApp = MembersApp;
