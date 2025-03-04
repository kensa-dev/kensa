import React from "react";
import './PackageName.scss';

const PackageName = ({fullPackageName, minimumUniquePackageName, packageDisplayMode}) => {
    
    const removeSuffix = (text, suffix) => suffix?.length > 0 ? text?.slice(0, -suffix.length) : text;
    const commonPackageName = () => removeSuffix(fullPackageName, minimumUniquePackageName);
    const withArrows = (packageName) => packageName.replaceAll(".", " > ");
    
    const title = `Package: ${fullPackageName}`;
    
    switch (packageDisplayMode) {
        case "Hidden":
            return null;
            
        case "HideCommonPackages":
            return minimumUniquePackageName 
                ? (
                    <div className="test-class-package-name" title={title}>
                        <span className="unique-package-name">{withArrows(minimumUniquePackageName)}</span>
                    </div>
                )
                : null;
            
        case "ShowFullPackage":
            if (fullPackageName.endsWith(minimumUniquePackageName)) {
                return (
                    <div className="test-class-package-name" title={title}>
                        <span className="common-package-name">{withArrows(commonPackageName())}</span>
                        <span className="unique-package-name">{withArrows(minimumUniquePackageName)}</span>
                    </div>
                );

            } else {
                return (
                    <div className="test-class-package-name" title={title}>
                        <span className="full-package-name">{withArrows(fullPackageName)}</span>
                    </div>
                );
            }

        default:
            console.warn("Unexpected value for data.packageDisplayMode:", packageDisplayMode);
            return null;
    }
}

export default PackageName;