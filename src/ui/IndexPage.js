import React, {Component} from 'react';
import './IndexPage.scss';
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {faSearch} from "@fortawesome/free-solid-svg-icons/faSearch";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const KENSA_FILTER_TYPE_KEY="KensaFilterType";
const KENSA_FILTER_VALUE_KEY="KensaFilterValue";

class Class extends Component {

    constructor(props, context) {
        super(props, context);

        this.state = {
            expanded: false
        };

        this.toggle = this.toggle.bind(this);
        this.icon = this.icon.bind(this);
        this.deriveClassFor = this.deriveClassFor.bind(this);
        this.isHidden = this.isHidden.bind(this);
        this.load = this.load.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {

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

    deriveClassFor(cls) {
        if (cls.matched && this.props.parentIsExpanded) {
            if (cls.state === "Passed" || this.props.filterType === "State" && this.props.filterValue === "Passed") {
                return "test-passed"
            }

            if (cls.state === "Failed" || this.props.filterType === "State" && this.props.filterValue === "Failed") {
                return "test-failed"
            }

            if (cls.state === "Disabled" || this.props.filterType === "State" && this.props.filterValue === "Disabled") {
                return "test-disabled"
            }
        }

        return "is-hidden"
    }

    isHidden() {
        return this.state.expanded ? "" : "is-hidden"
    }

    render() {
        const cls = this.props.cls;

        return (
                <dl className={this.deriveClassFor(cls)}>
                    <dt>
                        <a className="index-icon" onClick={this.toggle}><FontAwesomeIcon icon={this.icon()}/></a>
                        <a onClick={() => this.load()}>{cls.name}</a>
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
            expanded: this.props.pkg.expanded
        };

        this.toggle = this.toggle.bind(this);
        this.icon = this.icon.bind(this);
        this.deriveClassFor = this.deriveClassFor.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.pkg.expanded !== prevState.expanded) {
            this.setState({
                expanded: this.props.pkg.expanded
            })
        }
    }

    icon() {
        if (this.state.expanded) {
            return faMinus;
        }

        return faPlus;
    }

    toggle() {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }));
    }

    deriveClassFor(pkg) {
        if (pkg.matched && this.props.parentIsExpanded) {
            if (pkg.state === "Passed" || this.props.filterType === "State" && this.props.filterValue === "Passed") {
                return "test-passed"
            }

            if (pkg.state === "Failed" || this.props.filterType === "State" && this.props.filterValue === "Failed") {
                return "test-failed"
            }

            if (pkg.state === "Disabled" || this.props.filterType === "State" && this.props.filterValue === "Disabled") {
                return "test-disabled"
            }
        }

        return "is-hidden"
    }

    render() {
        const pkg = this.props.pkg;

        return <dl className={this.deriveClassFor(pkg)}>
            <dt>
                <a className="index-icon" onClick={this.toggle}><FontAwesomeIcon icon={this.icon()}/></a>
                {pkg.name}
            </dt>
            <dd>
                {
                    pkg.packages && pkg.packages.map((pkg) =>
                            <Package pkg={pkg}
                                     filterType={this.props.filterType}
                                     filterValue={this.props.filterValue}
                                     parentIsExpanded={this.state.expanded}/>
                    )
                }
                {
                    pkg.classes && pkg.classes.map((cls) =>
                            <Class cls={cls}
                                   filterType={this.props.filterType}
                                   filterValue={this.props.filterValue}
                                   parentIsExpanded={this.state.expanded}/>
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
            filterType: localStorage.getItem(KENSA_FILTER_TYPE_KEY) || "State",
            filterValue: localStorage.getItem(KENSA_FILTER_VALUE_KEY) || "All",
            filterMatched: false
        };

        this.onInputChanged = this.onInputChanged.bind(this);
        this.onStateFilterSelect = this.onStateFilterSelect.bind(this);
        this.stateFilter = this.stateFilter.bind(this);
        this.nameFilter = this.nameFilter.bind(this);
    }

    componentDidMount() {
        this.filterInput.focus();

        this.applyFilter()
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        let filterTypeChanged = this.state.filterType !== prevState.filterType;
        let filterValueChanged = this.state.filterValue !== prevState.filterValue;

        if (filterTypeChanged || filterValueChanged) {
            this.applyFilter()
        }
    }

    updateLocalStorage() {
        localStorage.setItem(KENSA_FILTER_TYPE_KEY, this.state.filterType);
        localStorage.setItem(KENSA_FILTER_VALUE_KEY, this.state.filterValue)
    }

    onInputChanged(e) {
        let value = e.target.value;
        if (value) {
            this.setState({
                filterType: "Name",
                filterValue: value
            }, () => this.updateLocalStorage());
        } else {
            this.setState({
                filterType: "State",
                filterValue: "All"
            }, () => this.updateLocalStorage());
        }
    }

    applyFilter() {
        if (this.state.filterType === "State") {
            this.setState({filterMatched: this.doApplyFilter(this.state.indices.packages, this.stateFilter)})
        } else if (this.state.filterType === "Name") {
            this.setState({filterMatched: this.doApplyFilter(this.state.indices.packages, this.nameFilter)})
        }
    }

    stateFilter(cls) {
        return (this.state.filterType === "State" && this.state.filterValue === "All") || this.state.filterValue === cls.state;
    }

    nameFilter(cls) {
        return cls.name.includes(this.state.filterValue);
    }

    doApplyFilter(packages, filter) {
        let matched = false;

        packages.forEach((pkg) => {
            pkg.matched = false;
            if (pkg.classes) {
                pkg.classes.forEach((cls) => {
                    pkg.matched = (cls.matched = filter(cls)) || pkg.matched;
                })
            }
            if (pkg.packages) {
                pkg.matched = this.doApplyFilter(pkg.packages, filter) || pkg.matched;
            }
            matched = matched || pkg.matched;
            pkg.expanded = pkg.matched
        });

        return matched;
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
                expanded: false,
                matched: false,
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
                    state: container.state,
                    expanded: false,
                    matched: false
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
            }, () => {this.updateLocalStorage()});
        }
    }

    stateFilterTabFor(value) {
        let cls = this.state.filterType === "State" && this.state.filterValue === value ? "is-active" : "";
        return <a className={cls} onClick={this.onStateFilterSelect}>{value}</a>
    }

    render() {
        const filterType = this.state.filterType;
        const filterValue = this.state.filterValue;
        const inputFilterValue = filterType === "State" ? "" : filterValue;

        return (
                <nav className="panel">
                    <div className="panel-block">
                        <p className="control has-icons-left">
                            <input ref={(input) => this.filterInput = input}
                                   className="input"
                                   value={inputFilterValue}
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
                                         parentIsExpanded={true}/>
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