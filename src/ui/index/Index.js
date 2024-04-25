import React, {useEffect, useReducer, useState} from 'react';
import './Index.scss';
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {createTree, treeReducer} from "./trees";
import Package from "./Package";
import {useNavigate, useSearchParams} from "react-router-dom";

const KENSA_FILTER_TEXT_REGEX = /\bissue:(?<issue>\w+-\d+,?)\b|(?<text>\b\w+\b)/g
const DEFAULT_FILTER = {issues: null, text: null, state: "All"}

const Index = () => {
    const [searchParams] = useSearchParams()
    const [filterText, setFilterText] = useState("")
    const [filter, setFilter] = useState(DEFAULT_FILTER);
    const [indexTree, applyFilter] = useReducer(treeReducer, {}, () => createTree(JSON.parse(document.querySelector("script[id='indices']").textContent).indices));
    const [isFilterValid, setFilterValid] = useState(true);

    const navigate = useNavigate();

    useEffect(() => {
        console.log("Apply filter", filter)
        applyFilter(filter)
    }, [filter]);

    useEffect(() => {
        const params = Object.fromEntries(searchParams);
        const issues = (params.issues?.length ? params.issues.split(",") : null)
        setFilterText( (issues?.length ? "issue:" + issues.join(",") : "") + (params.text || ""))
        setFilter({
            issues: (params.issues?.length ? params.issues.split(",") : null),
            text: (params.text?.length ? params.text : null),
            state: params.state || "All"
        })
    }, [searchParams]);


    const asQueryParams = (filter) => {
        const params = new URLSearchParams();
        for (let key in filter) {

             if (filter[key]) {
                 if (key === "issues") {
                     params.set(key, filter[key].join(","));
                 } else {
                     params.set(key, filter[key]);
                 }
            }
        }
        return params.toString();
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
            navigate('?' + asQueryParams({...filter, issues: (issues.length > 0 ? issues : null), text: texts[0]}))
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
        navigate('?' + asQueryParams(DEFAULT_FILTER))
        setFilterText("")
        setFilterValid(true)
    }

    const StateFilterTab = ({value}) =>
        <a className={filter.state === value ? "is-active" : ""}
           onClick={() => navigate("?" + asQueryParams({...filter, state: value}))}>{value}</a>

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
                    <StateFilterTab value={"All"}/>
                    <StateFilterTab value={"Passed"}/>
                    <StateFilterTab value={"Failed"}/>
                    <StateFilterTab value={"Disabled"}/>
                </p>
                <div>
                    {
                        (indexTree.matches) ?
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
        </section>
    </>
}
export default Index