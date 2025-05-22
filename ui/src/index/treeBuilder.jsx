const createClass = (container) => {
    return {
        name: container.displayName,
        state: container.state,
        fullClassName: container.testClass,
        isExpanded: false,
        isVisible: false,
        cssCls: null,
        issues: container.issues,
        tests: container.tests.map((test) => {
            return {
                name: test.displayName,
                method: test.testMethod,
                issues: (test.issues.length > 0) ? test.issues : container.issues,
                state: test.state,
                isVisible: true,
                cssCls: null
            }
        })
    }
}
const newPackage = (name) => {
    return {
        name: name,
        packages: [],
        classes: [],
        isExpanded: false,
        isVisible: false,
        cssCls: null
    }
}

const mapResult = (indices, pkgArray, container) => {
    let name = pkgArray.shift();

    if (pkgArray.length === 0) {
        indices["classes"].push(createClass(container))
    } else {
        const packages = indices["packages"]
        let pkg = packages.find(p => p.name === name)
        if (pkg === undefined) {
            pkg = newPackage(name)
            packages.push(pkg);
        }
        mapResult(pkg, pkgArray, container);
    }
}

export const createTree = (indices) => {
    let indexTree = {matches: true, packages: []}
    indices.forEach((testResult) => {
        mapResult(indexTree, testResult.testClass.split("."), testResult)
    });
    return indexTree
}