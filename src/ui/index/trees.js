const applyState = (pkg, state) => {
    if (pkg.state === "Failed" || pkg.state === "Disabled" || state === "NotExecuted" || state === "Disabled") {
        return;
    }

    if (state === "Failed" || state === "Passed") {
        return pkg.state = state;
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
            expanded: false,
            matched: false,
            issues: container.issues,
            tests: container.tests.map((test) => {
                return {
                    name: test.displayName,
                    method: test.testMethod,
                    issues: test.issues
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
        if (!pkg) {
            pkg = {
                name: name,
                state: container.state,
                expanded: false,
                matched: false
            };
            packages.push(pkg);
        } else {
            // applyState(pkg, container.state)
        }
        mapResult(pkg, pkgArray, container);
    }
}

const filterFor = (filter) => {
    switch (filter.type) {
        case "State":
            return (testClass) => (filter.type === "State" && filter.value === "All") || filter.value === testClass.state
        case "Name":
            return (testClass) => testClass.name.toLowerCase().includes(filter.value.toLowerCase())
        case "Issue":
            return (testClass) => testClass.issues.includes(filter.value.split(':')[1])
    }
}

const doApplyFilter = (packages, filterFn) => {
    return packages.map((pkg) => {
        pkg.matched = false;
        if (pkg.classes && pkg.classes.length > 0) {
            pkg.classes.forEach((cls) => {
                pkg.matched = (cls.matched = filterFn(cls)) || pkg.matched;
            })
        }
        if (pkg.packages && pkg.packages.length > 0) {
            pkg.packages = doApplyFilter(pkg.packages, filterFn);
        }
        pkg.expanded = pkg.matched
    });

}
export const filterTree = (indexTree, filter) => {
    const filterFn = filterFor(filter)

    console.log("indexTree", indexTree)
    let newTree = doApplyFilter(indexTree.packages, filterFn);
    console.log("newTree", indexTree)
    return newTree
}

export const createTree = (indices) => {
    let indexTree = {}
    indices.forEach((testResult) => {
        mapResult(indexTree, testResult.testClass.split("."), testResult)
    });
    return indexTree
}
