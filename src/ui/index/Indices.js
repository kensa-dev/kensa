import React, {useEffect, useReducer, useState} from 'react';
import './Indices.scss';
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {createTree, filterTree} from "./trees";
import Package from "./Package";

const KENSA_FILTER_KEY = "KensaFilter";
const KENSA_ISSUE_REGEXP = new RegExp(`^issue:([A-Za-z]{3,}-[0-9]+)$`, 'g')
const ALL_STATE_FILTER = {type: "State", value: "All"}

export default function Indices({indices}) {
    const [isFilterMatched, setFilterMatched] = useState(true);
    const [filter, setFilter] = useState(JSON.parse(localStorage.getItem(KENSA_FILTER_KEY)) || ALL_STATE_FILTER);
    const [indexTree, dispatch] = useReducer(filterTree, createTree(indices));

    useEffect(() => {
        localStorage.setItem(KENSA_FILTER_KEY, JSON.stringify(filter))

        console.log("useEffect1", indexTree)
        dispatch(filter)

        console.log("useEffect2", indexTree)
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
                    (isFilterMatched) ?
                        indexTree.packages && indexTree.packages.map((pkg, index) =>
                            <Package key={index} pkg={pkg} parentIsExpanded={true} filter={filter}/>
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