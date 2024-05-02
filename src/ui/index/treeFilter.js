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

const pkgCss = (hasFailures) => "idx-" + (hasFailures ? "failed" : "passed")

const testClassCss = (filter, classItem, anyTestsFailed) => "idx-" + (filter.state || (!anyTestsFailed ? "passed" : classItem.state)).toLowerCase().replaceAll(" ", "-")

const testCss = (filter, testItem) => "idx-" + (filter.state || testItem.state).toLowerCase().replaceAll(" ", "-")

const filterPackages = (packages, filter) => packages.reduce(([didMatch, didFail], pkg) => {
    const [anyMatches, anyFailures] = applyFilter(pkg, filter)
    pkg.isExpanded = pkg.isVisible = anyMatches
    pkg.cssCls = pkgCss(anyFailures)

    return [didMatch || anyMatches, didFail || anyFailures]
}, [false, false]);

const filterClasses = (classes, filter) => classes.reduce(([didMatch, didFail], classItem) => {
    const classMatches = new ClassMatches(filter, classItem)
    const [anyTestsMatched, anyTestsFailed] = filterTests(classItem, filter, classMatches)

    classItem.isVisible = anyTestsMatched || classMatches.matches
    classItem.cssCls = testClassCss(filter, classItem, anyTestsFailed)

    return [didMatch || anyTestsMatched, didFail || anyTestsFailed]
}, [false, false]);

const filterTests = (classItem, filter, classMatches) => classItem.tests.reduce(([didMatch, didFail], testItem) => {
    const testMatches = new TestMatches(filter, testItem, classMatches)
    testItem.isVisible = testMatches.matches
    testItem.cssCls = testCss(filter, testItem)

    return [didMatch || testItem.isVisible, didFail || (testItem.isVisible && testItem.state === "Failed")]
}, [false, false]);

const applyFilter = (item, filter) => {
    let [anyClassMatched, anyClassFailed] = filterClasses(item.classes, filter)
    let [anyPackageMatched, anyPackageFailed] = filterPackages(item.packages, filter)

    return [anyPackageMatched || anyClassMatched, anyPackageFailed || anyClassFailed]
}

export const treeReducer = (indexTree, filter) => {
    let didMatch = indexTree.packages.reduce((didMatch, pkg) => {
        const [anyMatched, anyFailed] = applyFilter(pkg, filter)
        pkg.isExpanded = pkg.isVisible = anyMatched
        pkg.cssCls = pkgCss(anyFailed)
        return didMatch || anyMatched
    }, false)

    return {...indexTree, matches: didMatch, packages: indexTree.packages}
}

