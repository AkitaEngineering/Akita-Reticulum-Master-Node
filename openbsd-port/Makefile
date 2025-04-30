# $OpenBSD$

COMMENT =   Reticulum network stack daemon and utilities (Akita Node)

# --- Basic Port Info ---
V =             0.9.4
DISTNAME =      reticulum-${V}
PKGNAME =       akita-rns-${V} # Using akita-rns as package name prefix
CATEGORIES =    net comms security

HOMEPAGE =      https://github.com/AkitaEngineering/akita-reticulum-master-node
# Upstream homepage remains Reticulum's official site:
# UPSTREAM_HOMEPAGE = https://reticulum.network/

# --- Source Code Location ---
MASTER_SITES =  https://github.com/markqvist/Reticulum/archive/refs/tags/
EXTRACT_SUFX =  .tar.gz # Confirmed format for v0.9.4 release

# --- Maintainer Info ---
MAINTAINER =    Akita Engineering <info@akitaengineering.com>

# --- Permissions ---
PERMIT_PACKAGE = Yes # OK to build packages from this port

# --- Dependencies ---
MODULES =       lang/python

# Runtime dependencies needed by Reticulum and the Akita helper scripts
# TODO: VERIFY 'comms/py-serial' PATH ON A LIVE OPENBSD SYSTEM! Check with pkg_info -Q
MODPY_RUN_DEPENDS = \
	security/py-cryptography \
	comms/py-serial \
	databases/py-yaml # Added for pf rule generator script

# --- Dedicated User/Group ---
# Creates user _reticulum:_reticulum for running rnsd
# TODO: PLACEHOLDER UID/GID - VERIFY AGAINST /usr/ports/infrastructure/db/user.list BEFORE COMMITTING
PORTS_USER =    _reticulum
PORTS_GROUP =   _reticulum
PORTS_UID =     902 # Placeholder - VERIFY!
PORTS_GID =     902 # Placeholder - VERIFY!

# --- Build & Installation ---
PKG_ARCH =      * # Pure Python or builds on all arches
MODPY_SETUPTOOLS = Yes

# Define installation paths used below
PREFIX =        /usr/local
EXAMPLE_DIR =   ${PREFIX}/share/examples/akita-rns # Changed pkg name prefix
HELPER_DIR =    ${PREFIX}/libexec/akita-rns      # Changed pkg name prefix

# Man pages to install (sources must be in ${FILESDIR})
MAN=            ${FILESDIR}/rnsd.8.sc \
		${FILESDIR}/reticulum-config.5.sc \
		${FILESDIR}/akita.7.sc

# Install examples, helper script, man pages in post-install stage
INSTALL_DATA_DIR += ${EXAMPLE_DIR}/reticulum-config \
			${EXAMPLE_DIR}/rc.d \
			${HELPER_DIR}

post-install:
	# Install sample config
	${INSTALL_DATA_DIR} ${EXAMPLE_DIR}/reticulum-config
	${INSTALL_DATA} ${WRKSRC}/packaging/openbsd/config.sample \
		${EXAMPLE_DIR}/reticulum-config/config.sample
	# Install rc.d script (substituting paths and names)
	${INSTALL_DATA_DIR} ${EXAMPLE_DIR}/rc.d
	sed -e 's,${PREFIX},/usr/local,g' \
		-e 's,\$${MODPY_VERSION},${MODPY_VERSION},g' \
		-e 's,libexec/akita/,libexec/akita-rns/,g' \
		-e 's,/etc/pf.anchors/rnsd,/etc/pf.anchors/akita-rnsd,g' \
		-e 's,logger -t rnsd,logger -t akita-rnsd,g' \
		${FILESDIR}/rnsd.rc.sample > ${WRKDIR}/akita-rnsd.rc
	# Use correct name for installed script example
	${INSTALL_SCRIPT} ${WRKDIR}/akita-rnsd.rc ${EXAMPLE_DIR}/rc.d/akita-rnsd
	# Install Python helper script for pf rules
	${INSTALL_DATA_DIR} ${HELPER_DIR}
	${INSTALL_SCRIPT} ${FILESDIR}/generate_pf_rules.py ${HELPER_DIR}/
	# Install man pages
	${INSTALL_MAN_DIR} ${PREFIX}/man/man5 ${PREFIX}/man/man7 ${PREFIX}/man/man8
	${INSTALL_MAN} ${FILESDIR}/rnsd.8.sc ${PREFIX}/man/man8/rnsd.8
	${INSTALL_MAN} ${FILESDIR}/reticulum-config.5.sc ${PREFIX}/man/man5/reticulum-config.5
	${INSTALL_MAN} ${FILESDIR}/akita.7.sc ${PREFIX}/man/man7/akita.7

# --- Testing ---
NO_TEST =       Yes # Disable 'make test' for now

.include <bsd.port.mk>

