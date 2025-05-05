# Akita Reticulum Master Node

**A robust, integrated Reticulum Network Stack (RNS) deployment for OpenBSD.**

This project provides the necessary components (OpenBSD port, rc.d service script, helper utilities, documentation) to install, configure, and manage the Reticulum Network Stack daemon (`rnsd`) as a first-class service on OpenBSD. It aims to create a reliable, secure, and manageable system suitable for acting as a core component in a Reticulum network – the "Akita Node."

**Current Status:** Beta

![Akita Reticulum Master Node](https://akitaengineering.com/wp-content/uploads/2025/05/AkitaReticulumMasterNode.png)

---

## Table of Contents

* [Introduction](#introduction)
* [Features](#features)
* [Target Platform](#target-platform)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
    * [From Ports Tree](#from-ports-tree)
    * [From Package (Future)](#from-package-future)
* [Configuration](#configuration)
    * [1. Reticulum Configuration](#1-reticulum-configuration)
    * [2. Firewall (pf) Anchor Setup](#2-firewall-pf-anchor-setup)
    * [3. Service Configuration](#3-service-configuration)
* [Usage](#usage)
* [Important Verification Notes](#important-verification-notes)
* [Documentation](#documentation)
* [Contributing](#contributing)
* [License](#license)
* [Disclaimer](#disclaimer)

---

## Introduction

[Reticulum](https://reticulum.network/) is a modern, cryptography-based network stack designed for resilience and independence. The Akita Reticulum Master Node project packages and integrates Reticulum (`rnsd` and associated tools) tightly into the OpenBSD operating system, leveraging its security focus, stability, and system administration tools like the ports collection, `rc.d`, and `pf`.

This allows organizations and individuals to deploy professional, purpose-driven Reticulum nodes that can function reliably as gateways, routers, firewalls, bridges, and servers within an RNS network. This project is maintained by Akita Engineering ([www.akitaengineering.com](https://www.akitaengineering.com)).

## Features

* **Easy Installation:** Packaged as a standard OpenBSD port (`net/akita-rns`).
* **Standard Service Management:** Includes an `rc.d` script (`/etc/rc.d/akita-rnsd`) for easy service control via `rcctl(8)`.
* **Dynamic Firewall Integration:** Automatically generates and loads basic `pf(4)` rules into a dedicated anchor (`akita-rnsd`) based on the enabled interfaces in the Reticulum configuration.
* **Centralized Configuration:** Uses standard OpenBSD locations (`/etc/reticulum/config`).
* **Enhanced Diagnostics:** Improved `rcctl status akita-rnsd` output including `rnsstatus` info; logs daemon output to `syslog(3)`.
* **Dedicated User:** Runs `rnsd` as an unprivileged user (`_reticulum`) created by the port.
* **Leverages Reticulum:** Builds upon RNS's core strengths: end-to-end encryption, mesh capabilities, multi-transport operation.
* **Leverages OpenBSD:** Benefits from OpenBSD's security track record, robust networking stack, and powerful `pf(4)` firewall.

## Target Platform

* **Operating System:** OpenBSD (intended for the latest stable release; requires testing).

## Prerequisites

* A running OpenBSD system.
* Root access for installation and configuration.
* **(Optional - for building from source)**: The OpenBSD ports tree checked out under `/usr/ports`. Basic familiarity with using the ports system.

## Installation

It is recommended to install using the OpenBSD ports tree.

### From Ports Tree

1.  **Place Port Files:** Copy the `openbsd-port` directory contents from this repository into `/usr/ports/net/akita-rns` on your OpenBSD system (you may need to create the `akita-rns` directory).
2.  **Verify Placeholders:** **Critically**, check and potentially modify the `Makefile` in `/usr/ports/net/akita-rns` to ensure the `comms/py-serial` path is correct for your system and that the UID/GID `902` is available in `/usr/ports/infrastructure/db/user.list`.
3.  **Update Ports Tree:** Ensure your `/usr/ports` tree is generally up-to-date.
4.  **Navigate & Install:**
    ```bash
    cd /usr/ports/net/akita-rns
    make makesum # Download source archive and record checksum
    # Recommended: make generate-plist # Generate an accurate PLIST
    make install clean
    ```
    This command will fetch dependencies, compile if necessary, create the `_reticulum` user/group, and install the `akita-rns` package.

### From Package (Future)

Once packages are built and available on mirrors:

```bash
pkg_add akita-rns
```

Configuration
After installation, follow these steps:

1. Reticulum Configuration
Create Storage Directory (if needed):
```
mkdir -p /var/reticulum/storage # Or your configured storage_path parent
chown _reticulum:_reticulum /var/reticulum /var/reticulum/storage
chmod 700 /var/reticulum /var/reticulum/storage
```
Copy Sample Config:
```
cp /usr/local/share/examples/akita-rns/reticulum-config/config.sample \
   /etc/reticulum/config
```
#### Edit Configuration: Modify /etc/reticulum/config according to your needs.

- Set sharing_scope: system (REQUIRED for status integration).

- Set storage_path: /var/reticulum/storage (or match created dir).

- Define your desired Reticulum interfaces (UDP, Serial, TUN, etc.).

- Refer to the reticulum-config(5) man page and the Official Reticulum Documentation.

### 2. Firewall (pf) Anchor Setup
#### Create Anchor Directory (if needed):

mkdir -p /etc/pf.anchors

Edit /etc/pf.conf: Add the following lines once:

# Anchor for Reticulum rules managed by rc.d script
anchor "akita-rnsd"
load anchor "akita-rnsd" from "/etc/pf.anchors/akita-rnsd"

Reload pf:
```
pfctl -f /etc/pf.conf
```
### 3. Service Configuration
Copy rc.d Script:
```
cp /usr/local/share/examples/akita-rns/rc.d/akita-rnsd /etc/rc.d/akita-rnsd
chmod +x /etc/rc.d/akita-rnsd
```
Edit /etc/rc.conf.local: Add the following lines to enable and configure the service:
```
akita_rnsd_flags="-c /etc/reticulum/config" # Or other rnsd flags
pkg_scripts="akita-rnsd" # Add service name to list of pkg scripts to start
```
## Usage

Manage the `rnsd` service using `rcctl(8)`:

### Service Management
- **Enable service:** `rcctl enable akita-rnsd`
- **Start service:** `rcctl start akita-rnsd`
- **Stop service:** `rcctl stop akita-rnsd`
- **Check status:** `rcctl status akita-rnsd`
- **Reload service:** `rcctl reload akita-rnsd`
- **Disable service:** `rcctl disable akita-rnsd`
- **Check config basics:** `rcctl check akita-rnsd`

Logs are typically stored in `/var/log/daemon` and tagged with `akita-rnsd`.

## Important Verification Notes

### User/Group ID
The port attempts to use UID/GID `902` (placeholder). **VERIFY** this ID is free in `/usr/ports/infrastructure/db/user.list`. Modify the `Makefile` if needed before building.

### `py-serial` Dependency
The port assumes `comms/py-serial`. **VERIFY** using `pkg_info -Q py-serial`. Modify the `Makefile` if needed.

### PLIST Generation
For a correct package:
1. Run `make generate-plist` in the port directory.
2. Ensure `make makesum` is executed before `make install`.

## Documentation

- **Man Pages:** `akita(7)`, `rnsd(8)`, `reticulum-config(5)`
- **This Repository:** Contains examples and setup details.
- **Package README:** Located at `/usr/local/share/doc/pkg-readmes/akita-rns-<version>`
- **Official Reticulum Documentation:** [Reticulum Manual](https://reticulum.network/manual/)

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

Licensed under the terms of the **BSD-2**. See the [`LICENSE`](LICENSE) file.

## Disclaimer

This software is provided **"as is"**, without warranty. Proper verification and thorough testing are required for usage on your target system.
