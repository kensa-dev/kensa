const createClass = (container) => {
    return {
        name: container.displayName,
        state: container.state,
        fullClassName: container.testClass,
        expanded: true,
        isVisible: true,
        cssCls: "idx-"+ container.state.toLowerCase(),
        issues: container.issues,
        tests: container.tests.map((test) => {
            return {
                name: test.displayName,
                method: test.testMethod,
                issues: (test.issues.length > 0) ? test.issues : container.issues,
                state: test.state,
                isVisible: true,
                cssCls: "idx-"+ test.state.toLowerCase()
            }
        })
    }
}
const newPackage = (name, container) => {
    return {
        name: name,
        state: container.state,
        expanded: true,
        isVisible: true,
        cssCls: "idx-"+ container.state.toLowerCase()
    }
}

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
        let c = createClass(container)
        indices["classes"] ? indices["classes"].push(c) : indices["classes"] = [c]
    } else {
        // It's a package name
        let packages = indices["packages"]
        if (!packages) {
            packages = []
            indices["packages"] = packages
        }

        let pkg = packages.find(p => p.name === name)
        if (pkg === undefined) {
            pkg = newPackage(name, container)
            packages.push(pkg);
        } else {
            applyState(pkg, container.state)
        }
        mapResult(pkg, pkgArray, container);
    }
}

export const createTree = (indices) => {
    let indexTree = {matches: true}
    indices.forEach((testResult) => {
        mapResult(indexTree, testResult.testClass.split("."), testResult)
    });
    return indexTree
}