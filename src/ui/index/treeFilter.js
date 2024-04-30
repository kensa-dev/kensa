

const filterIssues = (testClass, issues) => issues?.length > 0 ? issues.some(v => testClass.issues.includes(v)) : true
const filterText = (testClass, text) => text ? testClass.name.toLowerCase().includes(text.toLowerCase()) : true
const filterState = (testClass, state) => state ? state === testClass.state : true
const filterFor = (filter) => (testClass) =>
    filterIssues(testClass, filter.issues) &&
    filterText(testClass, filter.text) &&
    filterState(testClass, filter.state)


const applyFilter = (packages, filterFn) => {
    let matched = false;

    packages.forEach((pkg) => {
        pkg.matched = false;
        if (pkg.classes && pkg.classes.length > 0) {
            pkg.classes.forEach((cls) => {
                pkg.matched = (cls.matched = filterFn(cls)) || pkg.matched;
                if (cls.tests && cls.tests.length > 0) {
                    cls.tests.forEach((test) => {
                        let tstMatched = test.matched = filterFn(test);
                        cls.matched = tstMatched || cls.matched;
                        pkg.matched = tstMatched || pkg.matched;
                    })
                }
            })
        }
        if (pkg.packages && pkg.packages.length > 0) {
            pkg.matched = applyFilter(pkg.packages, filterFn) || pkg.matched;
        }
        matched = matched || pkg.matched;
        pkg.expanded = pkg.matched
    });

    return matched;
}

export const treeReducer = (indexTree, filter) => {
    const filterFn = filterFor(filter)

    const matches = applyFilter(indexTree.packages, filterFn);

    return {...indexTree, matches: matches, packages: indexTree.packages}
}

