.\" $OpenBSD: rnsd.8.sc,v 1.1 2025/04/26 ... $
.TH RNSD 8 "April 26, 2025" "OpenBSD" "System Manager's Manual"

.SH NAME
rnsd \- Reticulum Network Stack daemon

.SH SYNOPSIS
.SY rnsd
.OP \-c \ Ar config_file
.OP \-d
.OP \-v...
.OP \-q
.OP \-s
.OP \--exampleconfig
.OP \--version
.OP \-h

.SH DESCRIPTION
The
.NM
utility is the daemon process for the Reticulum Network Stack (RNS).
It initializes and manages the RNS interfaces defined in the configuration
file (see
.Xr reticulum-config 5 ),
handles cryptographic identity management, packet routing, resource proof
generation, announcement propagation, and provides the necessary backend
services for Reticulum applications running on the system or connecting via
Inter-Process Communication (IPC).

It is designed to run persistently, typically started at boot time via the
.Xr rc.d 8
framework if enabled in
.Xr rc.conf.local 8 .
When run as a service via the provided Akita rc.d script (`akita-rnsd`),
it usually runs as the
.Li _reticulum
user and logs output to
.Xr syslogd 8 .

.SH OPTIONS
.TP
.BI \-c \ Ar config_file , \ \-\-config \ Ar config_file
Specify the path to the Reticulum configuration directory or a specific
configuration file. If a directory is specified, Reticulum looks for a
.Pa config
file within it. Defaults to
.Pa ~/.reticulum/
for normal users, or typically uses
.Pa /etc/reticulum/config
when started as a system service via the Akita rc.d script.
.TP
.BI \-d
Daemonize. Detach from the controlling terminal and run
.NM
in the background. The standard Akita rc.d script does *not* use this flag,
as the
.Xr rc.d 8
framework handles daemonization more robustly.
.TP
.BI \-v , \ \-\-verbose
Increase verbosity level. Can be specified multiple times
(e.g., \fB-vv\fR, \fB-vvv\fR) for progressively more detailed logging output.
Useful for debugging connection or configuration issues. When run as a service,
output is directed to syslog.
.TP
.BI \-q , \ \-\-quiet
Decrease verbosity, showing only critical errors. Overrides
.Fl v .
.TP
.BI \-s , \ \-\-service
Run in service mode. Primarily adjusts logging behaviour, potentially
logging directly to files within the config directory instead of stdout/stderr
if not managed by a process supervisor like rc.d that handles redirection.
The Akita rc.d script handles logging via
.Xr logger 1 ,
so this flag is typically not needed when using the script.
.TP
.BI \-\-exampleconfig
Print a verbose example configuration file to standard output, including
explanations for many options and interface types, then exit.
.TP
.BI \-\-version
Show the Reticulum version number and exit.
.TP
.BI \-h , \ \-\-help
Show a brief help message listing command-line options and exit.

.SH SERVICE MANAGEMENT (OpenBSD rc.d using akita-rnsd)
When managed by the Akita rc.d script
.Pa /etc/rc.d/akita-rnsd :
.TP
.B Configuration
Set startup flags (like config file path) in
.Xr rc.conf.local 8
using the
.Li akita_rnsd_flags
variable (e.g., `akita_rnsd_flags="-c /etc/reticulum/config"`). The user can be
overridden with
.Li akita_rnsd_user .
Add `akita-rnsd` to `pkg_scripts` to enable startup on boot.
.TP
.B Control
Use
.Xr rcctl 8
to manage the service:
.Bd -literal -offset indent
rcctl enable akita-rnsd
rcctl disable akita-rnsd
rcctl start akita-rnsd
rcctl stop akita-rnsd
rcctl restart akita-rnsd
rcctl reload akita-rnsd  # Reloads pf rules, may send SIGHUP if rnsd supports it
rcctl status akita-rnsd  # Shows PID and detailed 'rnsstatus' output
rcctl check akita-rnsd   # Basic checks (config file existence, dependencies)
.Ed
.TP
.B Logging
Output is redirected via
.Xr logger 1
to
.Xr syslogd 8 ,
typically appearing in
.Pa /var/log/daemon
with the tag
.Li akita-rnsd
and facility
.Li daemon.info .
.TP
.B User
Runs as the
.Li _reticulum
user by default.
.TP
.B PF Integration
The script automatically generates rules based on the config and loads them
into the
.Li akita-rnsd
.Xr pf 4
anchor (requires setup in
.Pa /etc/pf.conf ).

.SH FILES
.TP
.Pa /etc/reticulum/config
Default system-wide configuration file location used by the Akita rc.d script.
.TP
.Pa /usr/local/share/examples/akita-rns/reticulum-config/config.sample
Sample configuration file installed by the port.
.TP
.Pa /etc/rc.d/akita-rnsd
The service control script (copied from examples).
.TP
.Pa /etc/pf.anchors/akita-rnsd
File containing dynamically generated pf rules, loaded into the 'akita-rnsd' anchor.
Managed by the rc.d script.
.TP
.Pa /var/reticulum/storage/
Typical location for identity keys, transport node information, etc., when run as the
.Li _reticulum
user (path must be configured in reticulum-config and be writable).
.TP
.Pa /var/run/akita-rnsd.pid
Likely PID file location when managed by rc.d (depends on rc.subr implementation).
.TP
.Pa /var/log/daemon
Typical location for logs via syslog.


.SH SEE ALSO
.Xr akita 7 ,
.Xr reticulum-config 5 ,
.Xr rnsstatus 1 ,
.Xr rnpath 1 ,
.Xr rnx 1 ,
.Xr pf 4 ,
.Xr pf.conf 5 ,
.Xr rc.conf.local 8 ,
.Xr rc.d 8 ,
.Xr rcctl 8 ,
.Xr logger 1 ,
.Xr syslogd 8

The official Reticulum website and manual: https://reticulum.network/

.SH AUTHOR
Reticulum was created by Markqvist (https://github.com/markqvist).
OpenBSD integration and packaging by Akita Engineering (www.akitaengineering.com).
