.\" $OpenBSD: reticulum-config.5.sc,v 1.1 2025/04/26 ... $
.TH RETICULUM-CONFIG 5 "April 26, 2025" "OpenBSD" "File Formats Manual"

.SH NAME
reticulum-config \- Reticulum Network Stack configuration file format

.SH DESCRIPTION
The Reticulum configuration file, typically located at
.Pa /etc/reticulum/config
for system-wide use with the Akita node, specifies the operational parameters
for the Reticulum daemon
.Pq rnsd
and related utilities. It defines communication interfaces, logging settings,
storage locations, and network behaviour.

The file uses YAML syntax (see https://yaml.org/). Indentation (usually 2 spaces)
is significant. Comments start with
.Li # .

.SH FILE FORMAT OVERVIEW
The configuration is a YAML dictionary with top-level keys for different
sections.

.SS Core Settings
.TP
.BI enable_transport : \ Ar boolean
If
.Li true
(default), this node will participate in the Reticulum transport network,
forwarding traffic for other nodes. Set to
.Li false
if this node should only communicate directly with peers or act solely as a client.
.TP
.BI transport_enabled : \ Ar boolean
Deprecated alias for
.Li enable_transport .
.TP
.BI sharing_scope : \ Ar scope
Determines how interface information is shared between RNS processes on the
same system. Must be set to
.Li system
(recommended for Akita node) to allow tools like
.Xr rnsstatus 1
to communicate with the daemon managed by
.Xr rc.d 8 . Other options like
.Li instance
(default if unspecified) limit sharing to the single process.
.TP
.BI storage_path : \ Ar path
Specifies the directory where Reticulum stores persistent data like cryptographic
identities, known destinations, transport information, etc. Defaults to
.Pa ~/.reticulum/storage/ .
For the Akita node service running as
.Li _reticulum ,
this might resolve to
.Pa /var/reticulum/storage/
if that user's home directory is set accordingly, or can be set explicitly here.
Ensure the specified path exists and is writable by the user running
.Xr rnsd 8 .
.TP
.BI logging_level : \ Ar level
Sets the default logging verbosity. Levels (from most to least verbose):
.Li debug , verbose , info , notice , warning , error , critical .
Defaults to
.Li notice .
Can be overridden by command-line flags
.Pq Fl v , Fl q .
.TP
.BI log_destination : \ Ar dest
Where logs should be sent. Options include
.Li stdout
(default) or a file path. When run via the Akita rc.d script, logs are piped to
.Xr syslogd 8
via
.Xr logger 1 ,
making this setting less relevant for service operation.

.SS Interfaces Section
This crucial section defines the communication channels Reticulum uses.
.TP
.BI interfaces : \ Ar list
A YAML list where each item is a dictionary defining one interface.
.RS
.TP
.BI type : \ Ar string
Required. Specifies the kind of interface. Common types listed below.
.TP
.BI enabled : \ Ar boolean
Optional. Set to
.Li true
(default) or
.Li false
to enable/disable this specific interface definition.
.TP
.BI name : \ Ar string
Optional. A user-friendly name for this interface shown in logs and status tools.
.TP
.BI ifac_netname : \ Ar string
Optional. Assigns the interface to a specific virtual network name. Interfaces
only communicate with others sharing the same network name (or if one/both have
no name). Useful for logically separating traffic over shared physical media.
.TP
.BI outgoing : \ Ar boolean
Optional. If
.Li true
(default), Reticulum will use this interface to send traffic. Set to
.Li false
to make it receive-only.
.TP
.BI mtu : \ Ar integer
Optional. Maximum Transmission Unit for this interface. Overrides default if set.
Careful adjustment needed based on underlying technology.
.TP
.BI pf_managed : \ Ar boolean
Optional (Akita specific interpretation). If set to
.Li false ,
the Akita rc.d script helper will *not* generate PF rules for this interface.
Defaults to
.Li true
(rules will be generated). Useful if you want to manage PF rules manually
for a specific interface.
.RE

.SS Common Interface Types
.TP
.BI AutoInterface
Uses UDP broadcasts/multicasts (typically port 4242) on a local network segment
to automatically discover and communicate with other RNS nodes.
.RS
.TP
.BI enabled : \ Ar boolean
.TP
.BI name : \ Ar string
.TP
.BI interface : \ Ar string
Optional. Specify the outgoing network interface name (e.g.,
.Li em0 , fxp0 ).
If omitted, Reticulum tries to auto-detect suitable interfaces.
.RE
.TP
.BI UDPInterface
Uses UDP unicast or multicast over IP networks.
.RS
.TP
.BI enabled : \ Ar boolean
.TP
.BI name : \ Ar string
.TP
.BI device : \ Ar string
Required. The underlying network interface name (e.g.,
.Li em0 ).
.TP
.BI port : \ Ar integer
Optional. UDP port number. Defaults to 4242.
.TP
.BI target_host : \ Ar string
Optional. If specified, sends packets via unicast to this IP address or hostname.
.TP
.BI target_port : \ Ar integer
Optional. Destination UDP port if using
.Li target_host .
Defaults to the value of
.Li port .
.TP
.BI group : \ Ar string
Optional. Multicast group address to join/send to.
.RE
.TP
.BI TCPInterface
Uses TCP for point-to-point links. More reliable but less efficient than UDP.
.RS
.TP
.BI enabled : \ Ar boolean
.TP
.BI name : \ Ar string
.TP
.BI target_host : \ Ar string
Specify IP/hostname to connect to.
.TP
.BI target_port : \ Ar integer
Specify TCP port to connect to.
.TP
.BI listen_port : \ Ar integer
Alternatively, specify a port to listen on for incoming TCP connections.
Cannot use both target_* and listen_port in the same definition.
.TP
.BI bind_ip : \ Ar string
Optional. IP address to bind to when listening. Defaults to all interfaces (0.0.0.0).
.RE
.TP
.BI SerialInterface
Uses a serial port (e.g., RS-232, USB-Serial). Common for packet radio (TNCs) or LoRa modules.
.RS
.TP
.BI enabled : \ Ar boolean
.TP
.BI name : \ Ar string
.TP
.BI port : \ Ar string
Required. Path to the serial device (e.g.,
.Pa /dev/cuaU0 ).
.TP
.BI speed : \ Ar integer
Required. Baud rate (e.g., 9600, 19200, 115200).
.TP
.BI databits , parity , stopbits :
Optional. Serial parameters (e.g., 8, 'N', 1). Defaults usually work.
.TP
.BI flow_control : \ Ar string
Optional. Set flow control ('hard' for RTS/CTS, 'soft' for XON/XOFF).
.RE
.TP
.BI TUNInterface
Creates a virtual network interface at the OS level (requires matching
.Xr tun 4
device). Can transport IP or Ethernet frames over Reticulum.
.RS
.TP
.BI enabled : \ Ar boolean
.TP
.BI name : \ Ar string
.TP
.BI device : \ Ar string
Required. Name of the corresponding OS TUN device (e.g.,
.Li tun0 ).
The Akita rc.d script does *not* automatically create this device; it must be
created manually or via
.Pa /etc/hostname.tun0 .
.TP
.BI mode : \ Ar string
Optional. Set to
.Li point_to_point
(default, IP),
.Li ethernet
(requires TAP device, not just TUN on OpenBSD), or other modes supported by RNS.
.TP
.BI virtual_ip , virtual_netmask :
Optional. Configure IP addressing directly on the interface if using IP modes.
.RE
.TP
.BI RNodeInterface
Communicates directly with RNode LoRa hardware via serial. Provides more control
than generic SerialInterface for RNode devices. Requires parameters like
.Li port , autodetect , frequency , bandwidth , spreading_factor , coding_rate .
Consult RNode documentation.

.SH EXAMPLES

.SS Basic LAN Node (Auto-Discovery)
This node will automatically find and connect to other RNS nodes on the same LAN segment.
.Bd -literal -offset indent
# /etc/reticulum/config
sharing_scope: system
enable_transport: true

interfaces:
  - type: AutoInterface
    name: lan_auto
    # Optional: specify interface if auto-detect fails or is ambiguous
    # interface: em0
.Ed

.SS LoRa Node via Serial (RNode or TNC)
Connects to a LoRa device (like an RNode flashed with RNode Firmware, or a generic TNC) attached via USB serial.
.Bd -literal -offset indent
# /etc/reticulum/config
sharing_scope: system
enable_transport: true
storage_path: /var/reticulum/storage # Ensure _reticulum can write here

interfaces:
  - type: SerialInterface
    name: lora_serial
    port: /dev/cuaU0  # Verify correct device for your hardware
    speed: 115200      # Adjust baud rate to match device
    # Optional: Add flow control etc. if needed
    # flow_control: hard
.Ed
.Em Note:
Ensure the
.Li _reticulum
user has read/write permissions on the serial device (e.g., add
.Li _reticulum
to the
.Li dialer
group in
.Pa /etc/group ).

.SS TUN Interface for IP over RNS (Basic VPN)
Creates a virtual tun0 interface for routing IP traffic over Reticulum.
.Bd -literal -offset indent
# /etc/reticulum/config
sharing_scope: system
enable_transport: true # Needed to route traffic for the TUN interface

interfaces:
  - type: TUNInterface
    name: rns_vpn
    device: tun0  # MUST correspond to an existing tun0 interface
                  # Create with 'ifconfig tun0 create' and persist in /etc/hostname.tun0
    # Configure IP settings either here or via ifconfig/hostname.tun0
    # virtual_ip: 10.99.0.1
    # virtual_netmask: 255.255.255.0
    mode: point_to_point # Or 'ethernet' if using a tap device (less common on OpenBSD)
  # Must have at least one other *physical* interface defined below
  # for Reticulum to actually send the TUN traffic over!
  - type: AutoInterface
    name: lan_transport_for_tun
.Ed
.Em Note:
The
.Li tun0
interface must be created and configured in OpenBSD separately (e.g., `ifconfig tun0 create && ifconfig tun0 up` and add `up` to `/etc/hostname.tun0`). The Akita rc.d script does *not* manage tun device creation.

.SS Combining Interfaces (Gateway Example)
Acts as a gateway between LoRa (Serial) and LAN (UDP).
.Bd -literal -offset indent
# /etc/reticulum/config
sharing_scope: system
enable_transport: true
storage_path: /var/reticulum/storage

interfaces:
  - type: SerialInterface
    name: lora_link
    port: /dev/cuaU0
    speed: 19200
    # Add RNode specific parameters if using RNodeInterface type

  - type: UDPInterface
    name: lan_link
    device: em0 # Or your LAN interface
    port: 4242
    pf_managed: true # Explicitly state PF rules should be generated (default)

  - type: TCPInterface
    name: management_listener
    listen_port: 4243 # Example for a specific management connection
    bind_ip: 127.0.0.1 # Only listen locally
    pf_managed: false # Manually configure PF for this specific port
.Ed

More examples can be generated using `rnsd --exampleconfig`.

.SH FILES
.TP
.Pa /etc/reticulum/config
Default system-wide configuration file path.
.TP
.Pa ~/.reticulum/config
Default user-specific configuration file location.
.TP
.Pa /usr/local/share/examples/akita-rns/reticulum-config/config.sample
Sample configuration file installed by the port.

.SH SEE ALSO
.Xr akita 7 ,
.Xr rnsd 8 ,
.Xr rnsstatus 1 ,
.Xr tun 4 ,
YAML specification (yaml.org)

Official Reticulum Manual: https://reticulum.network/manual/
