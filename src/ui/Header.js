import Logo from "-!react-svg-loader!./images/Logo.svg";
import React from "react";
import './Header.scss';
import './App.scss';

const Header = ({headerText, headerClass}) => {
    return (
        <section className={"header " + headerClass}>
            <div className="header-body">
                <a href={"index.html"}>
                    <Logo className="logo"/>
                </a>
                <h1 className="header-title">{headerText}</h1>
            </div>
        </section>
    )
}

export default Header;