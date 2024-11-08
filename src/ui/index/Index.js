import React, {useEffect, useReducer, useState} from 'react';
import './Index.scss';
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {treeReducer} from "./treeFilter";
import {createTree} from "./treeBuilder";
import Package from "./Package";
import {useNavigate, useSearchParams} from "react-router-dom";

const KENSA_FILTER_TEXT_REGEX = /\bissue:(?<issue>[0-9a-zA-Z\-]+,?)\b|(?<text>\b\w+\b)/g

const Index = () => {
    const [searchParams] = useSearchParams()
    const [filterText, setFilterText] = useState("")
    const [filter, setFilter] = useState({});
    const [indexTree, applyFilter] = useReducer(treeReducer, {}, () => createTree(JSON.parse(document.querySelector("script[id='indices']").textContent).indices));
    const [isFilterValid, setFilterValid] = useState(true);

    const navigate = useNavigate();

    useEffect(() => {
        applyFilter(filter)
    }, [filter]);

    useEffect(() => {
        const params = Object.fromEntries(searchParams);
        const issues = (params.issues?.length ? params.issues.split(",") : null)

        setFilterText(((issues?.map(i => "issue:" + i) || []).join(" ") + " " + (params.text || "")).trim())

        setFilter({
            issues: (params.issues?.length ? params.issues.split(",") : null),
            text: (params.text?.length ? params.text : null),
            state: params.state || null
        })
    }, [searchParams]);

    const setUrl = (filter) => {
        const params = new URLSearchParams();
        for (let key in filter) {
            if (filter[key]) {
                params.set(key, (key === "issues") ? filter[key].join(",") : filter[key])
            }
        }
        navigate({search: params.toString()});
    }

    function doFilter() {
        let matches = filterText.matchAll(KENSA_FILTER_TEXT_REGEX)
        let {issues, texts, isValid} = matches.reduce(({issues, texts, textCount}, match) => {
            if (match.groups?.issue) issues.push(match.groups.issue.split(',').map(i => i.trim()));
            if (match.groups?.text) {
                texts.push(match.groups.text)
                textCount++
            }

            isValid = textCount <= 1

            return {issues, texts, textCount, isValid}
        }, {issues: [], texts: [], textCount: 0, isValid: true})

        setFilterValid(isValid)

        if (isValid) {
            setUrl({...filter, issues: (issues.length > 0 ? issues : null), text: texts[0]})
        }
    }

    const onKeyDown = (e) => {
        switch (e.key) {
            case "Enter":
                doFilter();
                break;
            case "Escape":
                clearFilter();
                break;
        }
    }

    const updateFilterText = e => setFilterText(e.target.value);

    const clearFilter = () => {
        setUrl({})
        setFilterText("")
        setFilterValid(true)
    }

    const StateFilterTab = ({text, state}) =>
        <a className={filter.state === state ? "is-active" : ""}
           onClick={() => setUrl({...filter, state: state})}>{text}</a>

    return <>
        <section className="hero is-info is-light">
            <div className="hero-body">
                <h1 className="title">Index</h1>
            </div>
        </section>
        <section className="section">
            <nav className="block">
                <div className="field has-addons">
                    <p className="control">
                        <button className="button is-info is-light" onClick={clearFilter}>
                            <span className="icon is-small">
                                <i><FontAwesomeIcon icon={faTimesCircle}/></i>
                            </span>
                            <span>Clear</span>
                        </button>
                    </p>
                    <p className="control has-icons-left is-expanded">
                        <input autoFocus
                               className={"input" + (!isFilterValid ? " is-danger" : "")}
                               value={filterText}
                               type="text"
                               placeholder="filter"
                               onChange={updateFilterText}
                               onKeyDown={onKeyDown}/>
                        <span className="icon is-left"><FontAwesomeIcon icon={faSearch}/></span>
                    </p>
                    <p className="control">
                        <button className="button is-info" onClick={doFilter}>Search</button>
                    </p>
                </div>
                <p className="panel-tabs">
                    <StateFilterTab text={"All"} state={null}/>
                    <StateFilterTab text={"Passed"} state={"Passed"}/>
                    <StateFilterTab text={"Failed"} state={"Failed"}/>
                    <StateFilterTab text={"Disabled"} state={"Disabled"}/>
                </p>
                <div>
                    {
                        (indexTree.matches) ?
                            indexTree.packages && indexTree.packages.map((pkg, idx) =>
                                <Package key={idx} pkg={pkg} parentIsExpanded={true} filter={filter}/>
                            )
                            :
                            <div className="has-text-danger">
                                No tests match the filter!
                            </div>
                    }
                </div>

            </nav>
        </section>
    </>
}
export default Index