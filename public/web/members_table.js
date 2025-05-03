const btnClassAdd = "btn btn-primary btn-block";
const btnClassDel = "btn btn-danger btn-block";

/**
 * This is a stateless view showing the table header.
 */
function Header(props) {
    var ths = [];
    for (var groupName of props.members.get_group_names()) {
        // Determine group state (any plug in group is ON?)
        let anyOn = false;
        if (props.members.membership[groupName]) {
            for (let plug of props.members.membership[groupName]) {
                if (props.members.plugStates[plug] === "on") {
                    anyOn = true;
                    break;
                }
            }
        }
        let groupBtnClass = anyOn ? "btn-block btn-warning" : "btn-block btn-default";
        ths.push(
            <th key={groupName}>
                <button className={groupBtnClass} onClick={() => props.toggleGroup(groupName)}>
                    {groupName}
                </button>
                <button className={btnClassDel} onClick={() => props.removeGroup(groupName)}>
                    X
                </button>
            </th>
        );
    }

    return (
        <thead>
            <tr>
                <th rowSpan="2" width="10%">Members</th>
                <th colSpan={props.members.get_group_names().length}>Groups</th>
                <th rowSpan="2" width="10%">Remove from All Groups</th>
            </tr>
            <tr>
                {ths}
            </tr>
        </thead>
    );
}

/**
 * This is a stateless view showing one row of the table.
 */
function Row(props) {
    var members = props.members;
    // One checkbox cell per group:
    var tds = members.get_group_names().map(groupName => (
        <td key={groupName}>
            <input type="checkbox"
                   checked={members.is_member_in_group(props.memberName, groupName)}
                   onChange={e => props.toggleMember(props.memberName, groupName, e.target.checked)} />
        </td>
    ));

    // Color the plug name button based on its state (on=warning, off=default)
    let plugState = members.plugStates[props.memberName] || "off";
    let nameBtnClass = (plugState === "on") ? "btn-block btn-warning" : "btn-block btn-default";

    return (
        <tr>
            <td>
                <button className={nameBtnClass}>{props.memberName}</button>
            </td>
            {tds}
            <td>
                <button className={btnClassDel} onClick={() => props.removeMemberFromAll(props.memberName)}>
                    X
                </button>
            </td>
        </tr>
    );
}

/**
 * This is a stateless view showing the table body (all plug rows).
 */
function Body(props) {
    var rows = props.members.get_member_names().map(memberName => (
        <Row key={memberName}
             memberName={memberName}
             members={props.members}
             toggleMember={props.toggleMember}
             removeMemberFromAll={props.removeMemberFromAll} />
    ));
    return <tbody>{rows}</tbody>;
}

/**
 * This is a stateless view for the entire members table.
 */
function MembersTable(props) {
    if (props.members.get_group_names().length === 0) {
        return (<div>There are no groups.</div>);
    }
    return (
        <table className="table table-striped table-bordered">
            <Header members={props.members}
                    toggleGroup={props.toggleGroup}
                    removeGroup={props.removeGroup} />
            <Body members={props.members}
                  toggleMember={props.toggleMember}
                  removeMemberFromAll={props.removeMemberFromAll} />
        </table>
    );
}

// export
window.MembersTable = MembersTable;
