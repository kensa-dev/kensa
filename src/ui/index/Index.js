import React, {useMemo} from "react";
import Indices from "./Indices";

const Index = () => {
    const indices = useMemo(
        () => JSON.parse(document.querySelector("script[id='indices']").textContent).indices,
        []
    );

    return <div>
            <section className="hero is-info is-light">
                <div className="hero-body">
                    <h1 className="title">Index</h1>
                </div>
            </section>
            <section className="section">
                <Indices indices={indices}/>
            </section>
        </div>

}

export default Index