import Logo from '@/images/Logo.svg?react';
import React from "react";
import {GitHubIcon} from "@/Util";

const Header = ({titleText}) => {
    return (
        <nav className="navbar has-background-info-dark">
            <div className="navbar-brand p-3">
                <a href={"index.html"}>
                    <Logo className="logo"/>
                </a>
            </div>
            <div className="navbar-start">
                <div className="navbar-item has-text-centered">
                    <p className="title has-text-white">{titleText}</p>
                </div>
            </div>
            <div className="navbar-end">
                <span className="navbar-item">
                    <a className="button is-info is-inverted" target="_blank" href="https://github.com/kensa-dev/kensa">
                        <span className="icon"><GitHubIcon/></span>
                        <span>Kensa</span>
                    </a>
                </span>
            </div>
        </nav>
    )
}

export default Header;