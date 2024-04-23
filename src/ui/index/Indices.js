import React, {useEffect, useState} from 'react';
import './Indices.scss';
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {createTree} from "./trees";

const KENSA_FILTER_TYPE_KEY = "KensaFilterType";
const KENSA_FILTER_VALUE_KEY = "KensaFilterValue";
const KENSA_FILTER_ISSUE_KEY = "KensaFilterIssue";
const KENSA_ISSUE_REGEXP = new RegExp(`^issue:([A-Za-z]{3,}-[0-9]+)$`, 'g')

export const Class = ({cls, parentIsExpanded, filterType, filterValue}) => {
    const [isExpanded, setExpanded] = useState(false);

    const icon = () => isExpanded ? faMinus : faPlus

    const load = (anchor) => {
        let url = "./" + cls.container.testClass + ".html";
        if (anchor) {
            url += "#" + anchor;
        }
        window.location = url;
    }

    const toggle = () => {
        setExpanded(prev => !prev)
    }

    const deriveClassFor = (cls) => {
        if (cls.matched && parentIsExpanded) {
            if (cls.state === "Passed" || filterType === "State" && filterValue === "Passed") {
                return "idx-passed"
            }

            if (cls.state === "Failed" || filterType === "State" && filterValue === "Failed") {
                return "idx-failed"
            }

            if (cls.state === "Disabled" || filterType === "State" && filterValue === "Disabled") {
                return "idx-disabled"
            }
        }

        return "is-hidden"
    }

    const isHidden = () => isExpanded ? "" : "is-hidden"

    return (
        <dl className={deriveClassFor(cls)}>
            <dt>
                <span className="idx-icon" onClick={toggle}><FontAwesomeIcon icon={icon()}/></span>
                <a onClick={() => load()}>{cls.name}</a>
            </dt>
            {cls.tests.length > 0 &&
                cls.tests.map((entry, index) => {
                    return <dd key={index}>
                        <a onClick={() => load(entry.method)} className={isHidden()}>{entry.name}</a>
                    </dd>
                })
            }
        </dl>);
}

export const Package = ({pkg, parentIsExpanded, filterType, filterValue}) => {
    const [isExpanded, setExpanded] = useState(pkg.expanded);

    const icon = () => isExpanded ? faMinus : faPlus

    const toggle = () => {
        setExpanded(prev => !prev)
    }

    const deriveClassFor = (pkg) => {
        if (pkg.matched && parentIsExpanded) {
            if (pkg.state === "Passed" || filterType === "State" && filterValue === "Passed") {
                return "idx-passed"
            }

            if (pkg.state === "Failed" || filterType === "State" && filterValue === "Failed") {
                return "idx-failed"
            }

            if (pkg.state === "Disabled" || filterType === "State" && filterValue === "Disabled") {
                return "idx-disabled"
            }
        }

        return "is-hidden"
    }

    useEffect(() => {
        setExpanded(pkg.expanded)
    }, [pkg.expanded]);

    return <dl className={deriveClassFor(pkg)}>
        <dt>
            <span className="idx-icon" onClick={toggle}><FontAwesomeIcon icon={icon()}/></span>
            {pkg.name}
        </dt>
        <dd>
            {
                pkg.packages && pkg.packages.map((pkg, index) =>
                    <Package key={index} pkg={pkg}
                             filterType={filterType}
                             filterValue={filterValue}
                             parentIsExpanded={isExpanded}/>
                )
            }
            {
                pkg.classes && pkg.classes.map((cls, index) =>
                    <Class key={index} cls={cls}
                           filterType={filterType}
                           filterValue={filterValue}
                           parentIsExpanded={isExpanded}/>
                )
            }
        </dd>
    </dl>
}

export default function Indices({indices}) {
    const [isFilterMatched, setFilterMatched] = useState(false);
    const [filterType, setFilterType] = useState(localStorage.getItem(KENSA_FILTER_TYPE_KEY) || "State");
    const [filterValue, setFilterValue] = useState(localStorage.getItem(KENSA_FILTER_VALUE_KEY) || "All");
    const [indexTree, setIndexTree] = useState(createTree(indices));

    useEffect(() => {
        localStorage.setItem(KENSA_FILTER_TYPE_KEY, filterType)
        localStorage.setItem(KENSA_FILTER_VALUE_KEY, filterValue)
        applyFilter()
    }, [filterType, filterValue]);

    const getFirstGroup = (regexp, str) => {
        const array = [...str.matchAll(regexp)];
        return array.map(m => m[1]);
    }

    const onInputChanged = (e) => {
        let value = e.target.value;
        let issueFilter = getFirstGroup(KENSA_ISSUE_REGEXP, value)

        if (issueFilter.length) {
            setFilterType("Issue")
            setFilterValue(value)
        } else if (value) {
            setFilterType("Name")
            setFilterValue(value)
        } else {
            setFilterType("State")
            setFilterValue("All")
        }
    }

    const applyFilter = () => {
        console.log("applyFilter");
        switch (filterType) {
            case "State":
                setFilterMatched(doApplyFilter(indexTree.packages, stateFilter))
                break;
            case "Name":
                setFilterMatched(doApplyFilter(indexTree.packages, nameFilter))
                break;
            case "Issue":
                setFilterMatched(doApplyFilter(indexTree.packages, issueFilter))
                break;
        }
    }

    const stateFilter = (testClass) => (filterType === "State" && filterValue === "All") || filterValue === testClass.state

    const nameFilter = (testClass) => testClass.name.toLowerCase().includes(filterValue.toLowerCase())

    const issueFilter = (testClass) => testClass.issues.includes(filterValue.split(':')[1])

    const doApplyFilter = (packages, filter) => {
        let matched = false;

        packages.forEach((pkg) => {
            pkg.matched = false;
            if (pkg.classes) {
                pkg.classes.forEach((cls) => {
                    pkg.matched = (cls.matched = filter(cls)) || pkg.matched;
                })
            }
            if (pkg.packages) {
                pkg.matched = doApplyFilter(pkg.packages, filter) || pkg.matched;
            }
            matched = matched || pkg.matched;
            pkg.expanded = pkg.matched
        });

        return matched;
    }

    const showWhenFilterOffOrHasMatch = () => isFilterOffOrHasMatch() ? "panel-block" : "is-hidden"

    const hideWhenFilterOffOrHasMatch = () => isFilterOffOrHasMatch() ? "is-hidden" : "panel-block"

    const isFilterOffOrHasMatch = () => (filterType === "State" && filterValue === "All") || isFilterMatched

    const onStateFilterSelect = (e) => {
        let newType = e.target.textContent;
        if (newType !== filterType) {
            setFilterType("State")
            setFilterValue(e.target.textContent)
        }
    }

    const StateFilterTabFor = ({value}) => {
        let cls = filterType === "State" && filterValue === value ? "is-active" : "";
        return <a className={cls} onClick={onStateFilterSelect}>{value}</a>
    }

    return (
        <nav className="panel">
            <div className="panel-block">
                <p className="control has-icons-left">
                    <input autoFocus
                           className="input"
                           value={filterType === "State" ? "" : filterValue}
                           type="text"
                           placeholder="filter"
                           onChange={onInputChanged}/>
                    <span className="icon is-left"><FontAwesomeIcon icon={faSearch}/></span>
                </p>
            </div>
            <p className="panel-tabs">
                <StateFilterTabFor value={"All"}/>
                <StateFilterTabFor  value={"Passed"}/>
                <StateFilterTabFor  value={"Failed"}/>
                <StateFilterTabFor  value={"Disabled"}/>
            </p>
            <div className={showWhenFilterOffOrHasMatch()}>
                {indexTree.packages && indexTree.packages.map((pkg, index) =>
                    <Package key={index} pkg={pkg}
                             filterType={filterType}
                             filterValue={filterValue}
                             parentIsExpanded={true}/>
                )}
            </div>
            <div className={hideWhenFilterOffOrHasMatch()}>
                <div className="has-text-danger">
                    No tests match the filter!
                </div>
            </div>
        </nav>
    );
}