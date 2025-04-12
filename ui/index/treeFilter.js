class Matches {
    constructor(filter, item) {
        this.data = {
            hasTextMatches: this.textFilterFn(item, filter.text),
            hasIssueMatches: this.issueFilterFn(item, filter.issues),
            hasStateMatches: this.stateFilterFn(item, filter.state)
        }
    }

    textFilterFn = (item, text) => text ? item.name.toLowerCase().includes(text.toLowerCase()) : true
    issueFilterFn = (item, issues) => issues?.length > 0 ? issues.map(i => i.toLowerCase()).some(v => item.issues.map(i => i.toLowerCase()).includes(v)) : true
    stateFilterFn = (item, state) => state ? state === item.state : true
}

class TestMatches extends Matches {
    constructor(filter, tst, classMatches) {
        super(filter, tst);
        this.classMatches = classMatches

        this.matches = (this.data.hasTextMatches || this.classMatches.data.hasTextMatches) && (this.data.hasIssueMatches || this.classMatches.data.hasIssueMatches) && this.data.hasStateMatches
    }
}

class ClassMatches extends Matches {
    constructor(filter, cls) {
        super(filter, cls);

        this.matches = Object.values(this.data).every(v => v);
    }
}

const pkgCss = (anyFailed, anyDisabled, allNotExecuted) => "idx-" + (anyFailed ? "failed" : (anyDisabled ? "disabled" : (allNotExecuted ? "not-executed" : "passed")))

const classFor = (anyFailed, anyDisabled, allNotExecuted) => anyFailed ? "failed" : (anyDisabled ? "disabled" : (allNotExecuted ? "not-executed" : "passed"));

const testClassCss = (filter, classItem, anyFailed, anyDisabled, allNotExecuted) => "idx-" + (filter.state || classFor(anyFailed, anyDisabled, allNotExecuted))

const testCss = (filter, testItem) => "idx-" + (filter.state || testItem.state).toLowerCase().replaceAll(" ", "-")

const filterPackages = (packages, filter) => packages.reduce(([anyMatched, anyFailed, anyDisabled, allNotExecuted], pkg) => {
    const [anyPkgMatched, anyPkgFailed, anyPkgDisabled, allPkgNotExecuted] = applyFilter(pkg, filter)
    pkg.isExpanded = pkg.isVisible = anyPkgMatched
    pkg.cssCls = pkgCss(anyPkgFailed, anyPkgDisabled, allPkgNotExecuted)

    return [anyMatched || anyPkgMatched, anyFailed || anyPkgFailed, anyDisabled || anyPkgDisabled, allNotExecuted && allPkgNotExecuted]
}, [false, false, false, true]);

const filterClasses = (classes, filter) => classes.reduce(([anyMatched, anyFailed, anyDisabled, allNotExecuted], classItem) => {
    const classMatches = new ClassMatches(filter, classItem)
    const [anyTestsMatched, anyTestsFailed, anyTestsDisabled, allTestsNotExecuted] = filterTests(classItem, filter, classMatches)

    classItem.isVisible = anyTestsMatched || classMatches.matches
    classItem.cssCls = testClassCss(filter, classItem, anyTestsFailed, anyTestsDisabled, allTestsNotExecuted)

    return [anyMatched || anyTestsMatched, anyFailed || anyTestsFailed, anyDisabled || anyTestsDisabled, allNotExecuted && allTestsNotExecuted]
}, [false, false, false, true]);

const filterTests = (classItem, filter, classMatches) => classItem.tests.reduce(([anyMatched, anyFailed, anyDisabled, allNotExecuted], testItem) => {
    const testMatches = new TestMatches(filter, testItem, classMatches)
    testItem.isVisible = testMatches.matches
    testItem.cssCls = testCss(filter, testItem)

    return [anyMatched || testItem.isVisible, anyFailed || (testItem.isVisible && testItem.state === "Failed"), anyDisabled || (testItem.isVisible && testItem.state === "Disabled"), allNotExecuted && (testItem.isVisible ? testItem.state === "Not Executed" : true)]
}, [false, false, false, true]);

const applyFilter = (item, filter) => {
    let [anyClassMatched, anyClassFailed, anyClassDisabled, allClassesNotExecuted] = filterClasses(item.classes, filter)
    let [anyPackageMatched, anyPackageFailed, anyPackageDisabled, allPackagesNotExecuted] = filterPackages(item.packages, filter)

    return [anyPackageMatched || anyClassMatched, anyPackageFailed || anyClassFailed, anyPackageDisabled || anyClassDisabled, allPackagesNotExecuted && allClassesNotExecuted]
}

export const treeReducer = (indexTree, filter) => {
    let didMatch = indexTree.packages.reduce((didMatch, pkg) => {
        const [anyMatched, anyFailed, anyDisabled, allNotExecuted] = applyFilter(pkg, filter)
        pkg.isExpanded = pkg.isVisible = anyMatched
        pkg.cssCls = pkgCss(anyFailed, anyDisabled, allNotExecuted)
        return didMatch || anyMatched
    }, false)

    return {...indexTree, matches: didMatch, packages: indexTree.packages}
}

