import React, {useEffect, useState} from "react";
import Indices from "./Indices";

const Index = () => {
    const [indices, setIndices] = useState(null);

    useEffect(() => {
        setIndices(JSON.parse(document.querySelector("script[id='indices']").textContent).indices)
    }, []);

    if (indices) {
        return (
            <div>
                <section className="hero is-info is-light">
                    <div className="hero-body">
                        <h1 className="title">Index</h1>
                    </div>
                </section>
                <section className="section">
                    <Indices indices={indices}/>
                </section>
            </div>
        )
    }
}

export default Index