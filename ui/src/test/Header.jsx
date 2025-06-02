import Logo from '@/images/Logo.svg?react';
import React, {useContext} from "react";
import PackageName from "@/test/PackageName";
import Issues from "@/test/information/Issues";
import {GitHubIcon} from "@/Util";
import {ConfigContext} from "@/Util";

const Header = ({headerClass, testClass, displayName, issues}) => {
    const {indexUrl} = useContext(ConfigContext)

    return (
        <nav className={"navbar " + headerClass}>
            <div className="navbar-brand p-3">
                <a href={indexUrl}>
                    <Logo className="logo"/>
                </a>
            </div>
            <div className="navbar-start">
                <div className="navbar-item">
                    <div>
                        <PackageName testClass={testClass}/>
                        <p className="title has-text-white">{displayName}</p>
                        <div className="header-tags">
                            <Issues issues={issues}/>
                        </div>
                    </div>
                </div>
            </div>
            <div className="navbar-end">
                <span className="navbar-item">
                    <a className={"button " + headerClass} target="_blank" href="https://github.com/kensa-dev/kensa">
                        <span className="icon">
                            <GitHubIcon/>
                        </span>
                    </a>
                </span>
            </div>
        </nav>
    )
}

export default Header;