import importlib.machinery
import importlib.util
import sys
import io
from pathlib import Path

SCRIPT_PATH = Path(__file__).resolve().parents[0] / 'files' / 'generate_pf_rules.py'


def load_module():
    loader = importlib.machinery.SourceFileLoader('genpf', str(SCRIPT_PATH))
    spec = importlib.util.spec_from_loader(loader.name, loader)
    module = importlib.util.module_from_spec(spec)
    loader.exec_module(module)
    return module


def test_generates_udp_tun_and_tcp_rules(tmp_path, capsys):
    cfg = tmp_path / 'cfg.yaml'
    cfg.write_text(
        'interfaces:\n'
        '  - type: UDPInterface\n'
        '    name: testudp\n'
        '    enabled: true\n'
        '    port: 4242\n'
        '  - type: TUNInterface\n'
        '    name: tuntest\n'
        '    device: tun0\n'
        '  - type: TCPInterface\n'
        '    name: tcptest\n'
        '    listen_port: 9000\n'
        '    bind_ip: 127.0.0.1\n',
        encoding='utf-8'
    )

    module = load_module()
    module.generate_rules(str(cfg))

    captured = capsys.readouterr()
    out = captured.out
    err = captured.err

    assert 'pass in quick' in out
    assert 'proto udp' in out
    assert 'pass quick on tun0' in out
    assert 'proto tcp' in out
    assert 'port 9000' in out
    assert err == ''  # no warnings expected for these valid entries


def test_invalid_udp_port_emits_warning(tmp_path, capsys):
    cfg = tmp_path / 'cfg2.yaml'
    cfg.write_text(
        'interfaces:\n'
        '  - type: UDPInterface\n'
        '    name: invalidudp\n'
        '    enabled: true\n'
        '    port: bad\n',
        encoding='utf-8'
    )

    module = load_module()
    module.generate_rules(str(cfg))

    captured = capsys.readouterr()
    out = captured.out
    err = captured.err

    assert 'WARNING: Skipping UDPInterface' in err
    # ensure no pass lines created for the invalid interface
    assert 'pass in quick' not in out
