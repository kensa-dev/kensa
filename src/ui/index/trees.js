const applyState = (pkg, state) => {
    if (pkg.state === "Failed" || pkg.state === "Disabled" || state === "NotExecuted" || state === "Disabled") {
        return;
    }

    if (state === "Failed" || state === "Passed") {
        pkg.state = state;
    }
}

const mapResult = (indices, pkgArray, container) => {
    let name = pkgArray.shift();

    if (pkgArray.length === 0) {
        // It's a class name
        let clsArray = indices["classes"];
        if (!clsArray) {
            clsArray = [];
            indices["classes"] = clsArray;
        }
        let c = {
            name: container.displayName,
            state: container.state,
            fullClassName: container.testClass,
            expanded: true,
            matched: true,
            issues: container.issues,
            tests: container.tests.map((test) => {
                return {
                    name: test.displayName,
                    method: test.testMethod,
                    issues: (test.issues.length > 0) ? test.issues : container.issues,
                    state: test.state
                }
            })
        }
        clsArray.push(c);
    } else {
        // It's a package name
        let packages = indices["packages"];
        if (!packages) {
            packages = [];
            indices["packages"] = packages;
        }

        let pkg = packages.find(p => p.name === name);
        if (pkg === undefined) {
            pkg = {
                name: name,
                state: container.state,
                expanded: true,
                matched: true
            };
            packages.push(pkg);
        } else {
            applyState(pkg, container.state)
        }
        mapResult(pkg, pkgArray, container);
    }
}

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

export const createTree = (indices) => {
    let indexTree = {matches: true}
    indices.forEach((testResult) => {
        mapResult(indexTree, testResult.testClass.split("."), testResult)
    });
    return indexTree
}
