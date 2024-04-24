import React, {useEffect, useState} from 'react';
import './Indices.scss';
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {createTree} from "./trees";
import Package from "./Package";

const KENSA_FILTER_KEY = "KensaFilter";
const KENSA_ISSUE_REGEXP = new RegExp(`^issue:([A-Za-z]{3,}-[0-9]+)$`, 'g')
const ALL_STATE_FILTER = {type: "State", value: "All"}

export default function Indices({indices}) {
    const [isFilterMatched, setFilterMatched] = useState(false);
    const [filter, setFilter] = useState(JSON.parse(localStorage.getItem(KENSA_FILTER_KEY)) || ALL_STATE_FILTER);
    const [indexTree, setIndexTree] = useState(createTree(indices));

    useEffect(() => {
        localStorage.setItem(KENSA_FILTER_KEY, JSON.stringify(filter))

        setFilterMatched(doApplyFilter(indexTree.packages, filterFor(filter)))

    }, [filter]);

    const getFirstGroup = (regexp, str) => [...str.matchAll(regexp)].map(m => m[1])

    const onInputChanged = (e) => {
        let value = e.target.value;
        let issueFilter = getFirstGroup(KENSA_ISSUE_REGEXP, value)

        if (issueFilter.length) {
            setFilter({type: "Issue", value: value})
        } else if (value) {
            setFilter({type: "Name", value: value})
        } else {
            setFilter(ALL_STATE_FILTER)
        }
    }

    const filterFor = (filter) => {
        switch (filter.type) {
            case "State":
                return (testClass) => (filter.type === "State" && filter.value === "All") || filter.value === testClass.state
            case "Name":
                return (testClass) => testClass.name.toLowerCase().includes(filter.value.toLowerCase())
            case "Issue":
                return (testClass) => testClass.issues.includes(filter.value.split(':')[1])
        }
    }

    const doApplyFilter = (packages, filterFn) => {
        let matched = false;

        packages.forEach((pkg) => {
            pkg.matched = false;
            if (pkg.classes && pkg.classes.length > 0) {
                pkg.classes.forEach((cls) => {
                    pkg.matched = (cls.matched = filterFn(cls)) || pkg.matched;
                })
            }
            if (pkg.packages && pkg.packages.length > 0) {
                pkg.matched = doApplyFilter(pkg.packages, filterFn) || pkg.matched;
            }
            matched = matched || pkg.matched;
            pkg.expanded = pkg.matched
        });

        return matched;
    }

    const StateFilterTab = ({value}) => {
        let cls = filter.type === "State" && filter.value === value ? "is-active" : "";
        return <a className={cls} onClick={() => setFilter({type: "State", value: value})}>{value}</a>
    }

    return (
        <nav className="panel">
            <div className="panel-block">
                <p className="control has-icons-left">
                    <input autoFocus
                           className="input"
                           value={filter.type === "State" ? "" : filter.value}
                           type="text"
                           placeholder="filter"
                           onChange={onInputChanged}/>
                    <span className="icon is-left"><FontAwesomeIcon icon={faSearch}/></span>
                </p>
            </div>
            <p className="panel-tabs">
                <StateFilterTab value={"All"}/>
                <StateFilterTab value={"Passed"}/>
                <StateFilterTab value={"Failed"}/>
                <StateFilterTab value={"Disabled"}/>
            </p>
            <div className={"panel-block"}>
                {
                    ((filter.type === "State" && filter.value === "All") || isFilterMatched) ?
                    indexTree.packages && indexTree.packages.map((pkg, index) =>
                    <Package key={index} pkg={pkg} filter={filter} parentIsExpanded={true}/>
            )
            :
            <div className="has-text-danger">
                No tests match the filter!
            </div>
            }
        </div>

</nav>
)
}