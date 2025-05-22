import React, {useContext} from "react";
import './PackageName.scss';
import {ConfigContext} from "@/Util";

const PackageName = ({testClass}) => {
    const {packageDisplayMode} = useContext(ConfigContext)
    if (packageDisplayMode === "Hidden") return null;

    const fullPackageName = testClass.replace(/\.[^.]+$/, "");

    const generateBreadcrumbParts = () => {
        if (!fullPackageName) return [];

        const parts = fullPackageName.split(".");
        return parts.map((part) => ({
            name: part
        }));
    };

    const title = `Package: ${fullPackageName}`;
    const breadcrumbs = generateBreadcrumbParts();

    switch (packageDisplayMode) {
        case "HideCommonPackages":
            return (
                <nav className="breadcrumb is-small has-succeeds-separator" aria-label="breadcrumbs" title={title}>
                    <ul>
                        <li>
                            <a>
                                {breadcrumbs[breadcrumbs.length - 1]?.name}
                            </a>
                        </li>
                    </ul>
                </nav>
            );

        case "ShowFullPackage":
            return (
                <nav className="breadcrumb is-small has-succeeds-separator" aria-label="breadcrumbs" title={title}>
                    <ul>
                        {breadcrumbs.map((breadcrumb, index) => (
                            <li key={index}>
                                <a>{breadcrumb.name}</a>
                            </li>
                        ))}
                    </ul>
                </nav>
            );
    }
};

export default PackageName;
