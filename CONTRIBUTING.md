# Contributing to Akita Reticulum Master Node

Contributions are welcome! Here's how you can help:

## Reporting Bugs

* Check the [GitHub Issues](https://github.com/AkitaEngineering/akita-reticulum-master-node/issues) to see if the issue already exists.
* If not, open a new issue. Please include:
    * OpenBSD version you are using.
    * Akita Reticulum Master Node version (package version).
    * Reticulum version (`rnsd --version`).
    * Steps to reproduce the bug.
    * Relevant configuration snippets (anonymize sensitive data).
    * Relevant log output (`/var/log/daemon` or verbose `rnsd` output).

## Testing

* Testing the port build, installation, and runtime behavior on different OpenBSD versions (especially `-current`) is highly valuable. Report any issues found.
* Test different Reticulum interface types and configurations.
* Test the dynamic `pf` rule generation.

## Suggesting Features

* Open a GitHub issue describing the feature you'd like to see and why it would be beneficial.

## Submitting Pull Requests

* Fork the repository.
* Create a new branch for your feature or bugfix.
* Make your changes. Adhere to existing code style where possible.
* If adding features, consider adding documentation (man pages, README updates).
* Test your changes thoroughly on an OpenBSD system.
* Update the port `Makefile` version (`PKGREVISION`) if making functional changes.
* Push your branch and open a Pull Request against the `main` branch of the `AkitaEngineering/akita-reticulum-master-node` repository.
* Clearly describe the changes made in the Pull Request description.

Thank you for your interest in improving the Akita Reticulum Master Node! You can learn more about Akita Engineering at [www.akitaengineering.com](https://www.akitaengineering.com).
