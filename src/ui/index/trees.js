

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
            container: container,
            state: container.state,
            name: container.displayName,
            expanded: false,
            matched: false,
            issues: container.issues,
            tests: []
        };
        clsArray.push(c);
        container.tests.forEach((test) => {
            c.tests.push({
                name: test.displayName,
                method: test.testMethod,
                issues: test.issues
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
            applyState(pkg, container.state)
        }
        mapResult(pkg, pkgArray, container);
    }
}

export const createTree = (indices) => {
    let localIndexTree = {}
    indices.forEach((testResult) => {
        mapResult(localIndexTree, testResult.testClass.split("."), testResult)
    });
    return localIndexTree
}
