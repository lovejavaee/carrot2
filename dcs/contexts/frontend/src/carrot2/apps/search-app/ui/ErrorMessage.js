import "./ErrorMessage.css";

import React from "react";

import { branding } from "../../../config-branding.js";

export const ErrorMessage = props => {
  return (
    <div className="Error">
      {props.children}
      <p>That's all we know, sorry.</p>
    </div>
  );
};

export const GenericErrorMessage = ({ error, children }) => {
  return (
    <ErrorMessage error={error}>
      {children}
      <pre>{error && error.statusText.replace(/([&/?])/g, "$1\u200b")}</pre>
    </ErrorMessage>
  );
};

export const GenericSearchEngineErrorMessage = ({ error }) => {
  return (
    <GenericErrorMessage error={error}>
      <h2>Search engine error</h2>
      <p>Search could not be performed due to the following error:</p>
    </GenericErrorMessage>
  );
};

export const ClusteringServerRateLimitExceededError = () => {
  return (
    <div className="Error">
      <h2>Too many clustering requests</h2>

      <p>
        You are making too many clustering requests for our little demo
        server to handle. Please check back in a minute.
      </p>

      <p>
        <small className="light">
          {branding.createUnlimitedDistributionInfo()}
        </small>
      </p>
    </div>
  );
};

export const ClusteringRequestSizeLimitExceededError = () => {
  return (
    <div className="Error">
      <h2>Too much data to cluster</h2>

      <p>
        You sent too much data for our little demo
        server to handle. Lower the number of search results and try again.
      </p>

      <p>
        <small className="light">
          {branding.createUnlimitedDistributionInfo()}
        </small>
      </p>
    </div>
  );
};

export const ClusteringExceptionMessage = ({ exception }) => {
  return (
    <div className="Error">
      <h2>Clustering engine error</h2>

      <p>
        Results could not be clustered due to the following error:
      </p>

      <pre>
        {exception.stacktrace}
      </pre>

      <p>That's all we know.</p>
    </div>
  );
};

export const ClusteringErrorMessage = ({ message }) => {
  return (
      <ErrorMessage>
        <h2>Clustering engine error</h2>

        <p>
          Results could not be clustered due to the following error:
        </p>

        <pre>
          {message}
        </pre>
      </ErrorMessage>
  );
};

export const createClusteringErrorElement = error => {
  if (error && error.status === 429) {
    return <ClusteringServerRateLimitExceededError />;
  }

  if (error && error.status === 413) {
    return <ClusteringRequestSizeLimitExceededError />;
  }
  if (error && error.bodyParsed) {
    if (error.bodyParsed.stacktrace) {
      return <ClusteringExceptionMessage exception={error.bodyParsed}/>
    }
    if (error.bodyParsed.message) {
      return <ClusteringErrorMessage message={error.bodyParsed.message}/>
    }
  }

  return (
    <GenericErrorMessage error={error}>
      <h2>Clustering engine error</h2>
      <p>Results could not be clustered due to the following error:</p>
    </GenericErrorMessage>
  );
};