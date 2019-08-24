import React, {Component} from 'react';
import './IndexPage.scss';
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

class Class extends Component {

    constructor(props, context) {
        super(props, context);

        this.state = {
            matched: false,
            expanded: this.props.expanded
        };

        this.toggle = this.toggle.bind(this);
        this.icon = this.icon.bind(this);
        this.deriveClassFor = this.deriveClassFor.bind(this);
        this.isHidden = this.isHidden.bind(this);
        this.load = this.load.bind(this);
    }

    componentDidMount() {
        let filterType = this.props.filterType;
        let filterValue = this.props.filterValue;
        let cls = this.props.cls;

        this.applyFilter(filterType, filterValue, cls);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        let filterType = this.props.filterType;
        let filterTypeChanged = this.props.filterType !== prevProps.filterType;
        let filterValue = this.props.filterValue;
        let filterValueChanged = this.props.filterValue !== prevProps.filterValue;
        let cls = this.props.cls;

        if (filterTypeChanged || filterValueChanged) {
            this.applyFilter(filterType, filterValue, cls)
        }
    }

    applyFilter(filterType, filterValue, cls) {
        if (filterType === "State" && filterValue !== "All") {
            this.applyStateFilter(filterValue, cls)
        } else if (filterType === "Name") {
            this.applyNameFilter(filterValue, cls)
        }
    }

    applyStateFilter(filterValue, cls) {
        let state = cls.container.state;
        let matched = state === filterValue;
        this.setState({matched: matched, expanded: false});
        this.props.filterNotify(cls.name, matched ? 1 : 0);
    }

    applyNameFilter(filterValue, cls) {
        let name = cls.name;
        let matched = name.includes(filterValue);
        this.setState({matched: matched, expanded: false});
        this.props.filterNotify(name, matched ? 1 : 0);
    }

    icon() {
        if (this.state.expanded) {
            return faMinus;
        }

        return faPlus;
    }

    load(anchor) {
        let url = "./" + this.props.cls.container.testClass + ".html";
        if (anchor) {
            url += "#" + anchor;
        }
        window.location = url;
    }

    toggle() {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }));
    }

    classFor(state) {
        if (this.state.matched) {
            if (state === "Passed" || this.props.filterType === "State" && this.props.filterValue === "Passed") {
                return "link test-passed"
            }

            if (state === "Failed" || this.props.filterType === "State" && this.props.filterValue === "Failed") {
                return "link test-failed"
            }
        } else {
            return "is-hidden"
        }
    }

    deriveClassFor(cls) {
        return this.classFor(cls.state)
    }

    isHidden() {
        return this.state.expanded ? "" : "is-hidden"
    }

    render() {
        const cls = this.props.cls;
        const state = cls.state;

        return (
                <dl className={this.deriveClassFor(cls)}>
                    <dt>
                        <a className="index-icon" onClick={this.toggle}><FontAwesomeIcon icon={this.icon()}/></a>
                        <a className={this.classFor(state)} onClick={this.load}>{cls.name}</a>
                    </dt>
                    {cls.tests.length > 0 &&
                    cls.tests.map((entry) => {
                        return <dd>
                            <a onClick={() => this.load(entry.method)} className={this.isHidden()}>{entry.name}</a>
                        </dd>
                    })
                    }
                </dl>);
    }
}

class Package extends Component {

    constructor(props, context) {
        super(props, context);

        this.state = {
            expanded: false,
            matchCount: -1,
            matchCounts: []
        };

        this.toggle = this.toggle.bind(this);
        this.icon = this.icon.bind(this);
        this.filterNotify = this.filterNotify.bind(this);
        this.deriveClassFor = this.deriveClassFor.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.matchCount !== this.state.matchCount) {
            this.props.filterNotify(this.props.pkg.name, this.state.matchCount)
        }

        // if (filterTypeChanged) {
        //     this.setState({
        //         matchCount: -1,
        //         matchCounts: []
        //     });
        // }
    }

    icon() {
        if (this.state.expanded || this.state.matchCount > 0) {
            return faMinus;
        }

        return faPlus;
    }

    toggle() {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }));
    }

    filterNotify(childName, count) {
        let index = this.state.matchCounts.findIndex(v => v.childName === childName);
        if (index < 0) {
            this.state.matchCounts.push({childName: childName, count: count})
        } else {
            this.state.matchCounts[index].count = count
        }

        this.setState(prevState => ({
            matchCount: prevState.matchCounts
                    .map(v => v.count)
                    .reduce((a, v) => a + v)
        }))
    }

    classFor(state) {
        if (state === "Passed" || this.props.filterType === "State" && this.props.filterValue === "Passed") {
            return "test-passed"
        }

        if (state === "Failed" || this.props.filterType === "State" && this.props.filterValue === "Failed") {
            return "test-failed"
        }
    }

    deriveClassFor(pkg) {
        if (this.state.matchCount > 0 || this.props.root || this.props.parentIsExpanded) {
            return this.classFor(pkg.state)
        }

        return "is-hidden"
    }

    render() {
        const pkg = this.props.pkg;
        const state = pkg.state;

        return <dl className={this.deriveClassFor(pkg)}>
            <dt className={this.classFor(state)}>
                <a className="index-icon" onClick={this.toggle}><FontAwesomeIcon icon={this.icon()}/></a>
                {pkg.name}
            </dt>
            <dd>
                {
                    pkg.packages && pkg.packages.map((pkg) =>
                            <Package pkg={pkg}
                                     filterType={this.props.filterType}
                                     filterValue={this.props.filterValue}
                                     parentIsExpanded={this.state.expanded}
                                     filterNotify={this.filterNotify}/>
                    )
                }
                {
                    pkg.classes && pkg.classes.map((cls) =>
                            <Class cls={cls}
                                   filterType={this.props.filterType}
                                   filterValue={this.props.filterValue}
                                   parentIsExpanded={this.state.expanded}
                                   filterNotify={this.filterNotify}/>
                    )
                }
            </dd>
        </dl>
    }
}

export default class Indices extends Component {

    constructor(props, context) {
        super(props, context);

        let indices = {};

        this.props.indices.forEach((testResult) => {
            this.mapResult(indices, testResult.testClass.split("."), testResult)
        });

        this.state = {
            indices: indices,
            filterType: "State",
            filterValue: "All",
            filterMatched: false
        };

        this.onInputChanged = this.onInputChanged.bind(this);
        this.filterNotify = this.filterNotify.bind(this);
        this.onStateFilterSelect = this.onStateFilterSelect.bind(this);
    }

    componentDidMount() {
        this.filterInput.focus()
    }

    onInputChanged(e) {
        let value = e.target.value;
        if (value) {
            this.setState({
                filterType: "Name",
                filterValue: value
            })
        } else {
            this.setState({
                filterType: "State",
                filterValue: "All"
            })
        }
    }

    filterNotify(childName, count) {
        this.setState({
            filterMatched: count > 0
        })
    }

    applyState(pkg, state) {
        if (pkg.state === "Failed" || pkg.state === "Disabled" || state === "NotExecuted" || state === "Disabled") {
            return;
        }

        if (state === "Failed" || state === "Passed") {
            return pkg.state = state;
        }
    }

    mapResult(indices, pkgArray, container) {
        let name = pkgArray.shift();
        if (pkgArray.length === 0) {
            // It's a class name
            let clsArray = indices["classes"];
            if (!clsArray) {
                clsArray = [];
                indices["classes"] = clsArray;
            }
            let c = {
                container: container,
                state: container.state,
                name: container.displayName,
                tests: []
            };
            clsArray.push(c);
            container.tests.forEach((test) => {
                c.tests.push({
                    name: test.displayName,
                    method: test.testMethod
                })
            })
        } else {
            // It's a package name
            let packages = indices["packages"];
            if (!packages) {
                packages = [];
                indices["packages"] = packages;
            }

            let pkg = packages.find(p => p.name === name);
            if (!pkg) {
                pkg = {
                    name: name,
                    state: container.state
                };
                packages.push(pkg);
            } else {
                this.applyState(pkg, container.state)
            }
            this.mapResult(pkg, pkgArray, container);
        }
    }

    showWhenFilterOffOrHasMatch() {
        if (this.isFilterOffOrHasMatch()) {
            return "panel-block"
        } else {
            return "is-hidden"
        }
    }

    hideWhenFilterOffOrHasMatch() {
        if (this.isFilterOffOrHasMatch()) {
            return "is-hidden"
        } else {
            return "panel-block"
        }
    }

    isFilterOffOrHasMatch() {
        let filterValue = this.state.filterValue;
        let filterType = this.state.filterType;

        return (filterType === "State" && filterValue === "All") ||
                this.state.filterMatched;
    }

    onStateFilterSelect(e) {
        let newType = e.target.textContent;
        if (newType !== this.state.filterType) {
            this.setState({
                filterType: "State",
                filterValue: e.target.textContent
            })
        }
    }

    stateFilterTabFor(value) {
        let cls = this.state.filterType === "State" && this.state.filterValue === value ? "is-active" : "";
        return <a className={cls} onClick={this.onStateFilterSelect}>{value}</a>
    }

    render() {
        const filterType = this.state.filterType;
        const filterValue = this.state.filterValue;

        return (
                <nav className="panel">
                    <div className="panel-block">
                        <p className="control has-icons-left">
                            <input ref={(input) => this.filterInput = input}
                                   className="input"
                                   type="text"
                                   placeholder="filter"
                                   onChange={this.onInputChanged}/>
                            <span className="icon is-left"><FontAwesomeIcon icon={faSearch}/></span>
                        </p>
                    </div>
                    <p className="panel-tabs">
                        {this.stateFilterTabFor("All")}
                        {this.stateFilterTabFor("Passed")}
                        {this.stateFilterTabFor("Failed")}
                        {this.stateFilterTabFor("Disabled")}
                    </p>
                    <div className={this.showWhenFilterOffOrHasMatch()}>
                        {this.state.indices.packages && this.state.indices.packages.map((pkg) =>
                                <Package pkg={pkg}
                                         filterType={filterType}
                                         filterValue={filterValue}
                                         filterNotify={this.filterNotify}
                                         root={true}/>
                        )}
                    </div>
                    <div className={this.hideWhenFilterOffOrHasMatch()}>
                        <div className="has-text-danger">
                            No tests match the filter!
                        </div>
                    </div>
                </nav>
        );
    }
}