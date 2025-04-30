.\" $OpenBSD: akita.7.sc,v 1.1 2025/04/26 ... $
.TH AKITA 7 "April 26, 2025" "OpenBSD" "Miscellaneous Information Manual"

.SH NAME
akita \- Akita Reticulum Master Node overview

.SH DESCRIPTION
The Akita Reticulum Master Node project, by Akita Engineering, provides a
robust, integrated deployment of the Reticulum Network Stack (RNS) on OpenBSD.
It aims to create a reliable, secure, and manageable system suitable for acting as a
core component ("Master Node") in a Reticulum network, fulfilling roles such as:
.Item Gateway/Bridge: Connecting different network segments or transport types (e.g., LoRa to Ethernet).
.Item Router: Participating in the transport network to relay traffic for others.
.Item Firewall: Controlling traffic flow using OpenBSD's
.Xr pf 4 .
.Item Server: Hosting RNS-based applications and services reliably.

This integration is achieved by packaging Reticulum using the OpenBSD ports system
.Pq Pa /usr/ports ,
providing a standard service management script for
.Xr rc.d 8 ,
and integrating with system facilities like
.Xr pf 4
and
.Xr syslogd 8 .
The goal is to combine the strengths of Reticulum (cryptographic identity, resilience, mesh networking)
with the strengths of OpenBSD (security focus, stability, mature system tools).

.SH COMPONENTS
The core components installed by the associated package (e.g.,
.Li akita-rns )
include:
.TP
.B rnsd(8)
The Reticulum daemon process. Manages interfaces, routing, cryptography.
.TP
.B Reticulum Utilities
Command-line tools:
.Xr rnsstatus 1
(check status),
.Xr rnpath 1
(discover paths),
.Xr rnx 1
(execute commands remotely), etc.
.TP
.B Python Libraries
The underlying RNS Python library (`import RNS`) used by the daemon and tools.
.TP
.B rc.d Script
.Pa /etc/rc.d/akita-rnsd
(copied from examples) provides standard service management via
.Xr rcctl 8 .
It handles startup, shutdown, status checks, logging redirection, and dynamic
.Xr pf 4
rule generation.
.TP
.B Configuration Files
System-wide configuration in
.Pa /etc/reticulum/config . See
.Xr reticulum-config 5 . Sample provided in examples directory.
.TP
.B Dynamic PF Rules Helper
.Pa /usr/local/libexec/akita-rns/generate_pf_rules.py
A Python script called by the rc.d script to parse the Reticulum config and
generate basic firewall rules. Requires the
.Li databases/py-yaml
package.
.TP
.B Dedicated User
.Li _reticulum
An unprivileged user created by the port, used to run the
.Xr rnsd 8
daemon.
.TP
.B Storage Directory
Persistent data like keys and routing info, typically
.Pa /var/reticulum/storage/
for the
.Li _reticulum
user (ensure path is configured and writable).


.SH TYPICAL USE CASES
.Item Connecting a local LoRa network segment to a wider RNS network over Ethernet/Internet.
.Item Providing a stable, always-on transport node for a community mesh network.
.Item Hosting an LXMF message store-and-forward service.
.Item Acting as a secure entry/exit point for an internal RNS network, using
.Xr pf 4
for policy enforcement.
.Item Running custom monitoring or control applications using RNS for transport.

.SH SETUP
Detailed setup instructions are in the package README:
.Pa /usr/local/share/doc/pkg-readmes/akita-rns-<version>
Key steps involve:
.Numbered
.It Installing the package (e.g., `make install clean` in the port dir).
.It Verifying UID/GID and dependency paths (see README / Makefile).
.It Creating and configuring the storage directory.
.It Copying and editing
.Pa /etc/reticulum/config .
.It Configuring the
.Li akita-rnsd
anchor in
.Pa /etc/pf.conf .
.It Copying the rc.d script to
.Pa /etc/rc.d/ .
.It Enabling and configuring the service in
.Pa /etc/rc.conf.local .
.It Starting the service using
.Xr rcctl 8 .

.SH SECURITY CONSIDERATIONS
.Item Akita leverages OpenBSD's security features (`pf`, dedicated user).
.Item Reticulum provides end-to-end encryption based on cryptographic identities.
.Item The automatically generated `pf` rules are basic (`pass quick...`). Review them
carefully in
.Pa /etc/pf.anchors/akita-rnsd
and consider implementing more restrictive rules directly in
.Pa /etc/pf.conf
if needed.
.Item Ensure the
.Li _reticulum
user has appropriate (minimal) permissions, especially regarding access to
serial ports or network interfaces if not managed solely by root/kernel. Add to `dialer` group for serial access.
.Item Ensure the
.Li _reticulum
user's storage directory (e.g.,
.Pa /var/reticulum/storage/ )
has appropriate permissions (e.g., `chmod 700 /var/reticulum/storage`).
.Item Keep the underlying OpenBSD system and the Reticulum package updated.

.SH SEE ALSO
.Xr reticulum-config 5 ,
.Xr rnsd 8 ,
.Xr rnsstatus 1 ,
.Xr pf 4 ,
.Xr pf.conf 5 ,
.Xr rc.d 8 ,
.Xr rcctl 8 ,
.Xr ports 7

Akita Engineering: https://www.akitaengineering.com
Official Reticulum Documentation: https://reticulum.network/manual/

.SH AUTHOR
Akita Engineering (www.akitaengineering.com).
Reticulum created by Markqvist.

.SH IMPORTANT NOTES
See the package README and Makefile comments regarding essential verification steps for
UID/GID and dependency paths before finalizing the port for local use or contribution.
