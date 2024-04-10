import React from "react";
import SearchBar from "./SearchBar";
import Owl from './Owl.png'
const NotFound=(NotFoundQuery)=>{


    return(
        <div className="not-found-page">
            <SearchBar query={NotFoundQuery}/>
            <div className="not-found-container">
            No results containing all your search terms were found.
            Your search -<i>{NotFoundQuery}</i> - did not match any documents.
            <b>Suggestions:</b>
            - Make sure that all words are spelled correctly.
            - Try different keywords.
            - Try more general keywords.
            </div>
        </div>
    );
}

export default NotFound;