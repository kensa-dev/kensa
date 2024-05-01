class Matches {
    constructor(filter, item) {
        this.data = {
            hasTextMatches: this.textFilterFn(item, filter.text),
            hasIssueMatches: this.issueFilterFn(item, filter.issues),
            hasStateMatches: this.stateFilterFn(item, filter.state)
        }
    }

    textFilterFn = (item, text) => text ? item.name.toLowerCase().includes(text.toLowerCase()) : true
    issueFilterFn = (item, issues) => issues?.length > 0 ? issues.some(v => item.issues.includes(v)) : true
    stateFilterFn = (item, state) => state ? state === item.state : true
}

class TestMatches extends Matches {
    constructor(filter, tst, classMatches) {
        super(filter, tst);
        this.classMatches = classMatches

        this.isVisible = (this.data.hasTextMatches || this.classMatches.data.hasTextMatches) && (this.data.hasIssueMatches || this.classMatches.data.hasIssueMatches) && this.data.hasStateMatches
    }
}

class ClassMatches extends Matches {
    constructor(filter, cls) {
        super(filter, cls);

        this.isVisible = Object.values(this.data).every(v => v);
    }
}

const applyFilter = (item, filter) => {
    let hasMatches = false
    if (item.packages && item.packages.length > 0) {
        item.packages.forEach((pkg) => {
            pkg.expanded = pkg.isVisible = hasMatches = applyFilter(pkg, filter)
            pkg.cssCls = "idx-" + (filter.state || pkg.state).toLowerCase().replaceAll(" ", "-")
        });
    }

    (item.classes || []).forEach((cls) => {
        const clsMatches = new ClassMatches(filter, cls)
        const anyTestsMatched = (cls.tests || []).reduce((didMatch, tst) => {
            const tstMatches = new TestMatches(filter, tst, clsMatches)

            tst.isVisible = tstMatches.isVisible
            tst.cssCls = "idx-" + (filter.state || tst.state).toLowerCase().replaceAll(" ", "-")
            return tst.isVisible
        }, false)

        if (anyTestsMatched) {
            cls.isVisible = true
            hasMatches = true
            cls.cssCls = "idx-" + (filter.state || cls.state).toLowerCase().replaceAll(" ", "-")
        } else {
            cls.isVisible = clsMatches.isVisible

            hasMatches = hasMatches || cls.isVisible
            cls.cssCls = "idx-" + (filter.state || cls.state).toLowerCase().replaceAll(" ", "-")
        }
    })

    return hasMatches
}

export const treeReducer = (indexTree, filter) => {
    let didMatch = indexTree.packages.reduce((didMatch, pkg) => {
        pkg.expanded = pkg.isVisible = applyFilter(pkg, filter);
        pkg.cssCls = "idx-" + (filter.state || pkg.state).toLowerCase().replaceAll(" ", "-")
        return didMatch || pkg.isVisible
    }, false)

    return {...indexTree, matches: didMatch, packages: indexTree.packages}
}

