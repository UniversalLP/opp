import platform
import subprocess

os = platform.system()
latency = 0
last_latency = -1
packet_loss = 0


def can_run():
    return os.lower() == "linux" or os.lower() == "windows"


def ping_ip(ip_addr):
    global latency
    global packet_loss
    global last_latency
    last_latency = latency
    if os.lower() == "linux":
        out = subprocess.Popen(["ping", ip_addr, "-c", "1"], stdout=subprocess.PIPE).communicate()[0]
        cut = out.split("=")[3]
        lat = cut.split(" ")[0]
        latency = lat.split(".")[0]
        cut = out.split(", ")[2]
        loss = cut.split("%")[0]

        if loss == "0":
            packet_loss = "0"
        else:
            packet_loss = "1"
    elif os.lower() == "windows":
        out = subprocess.Popen(["ping.exe", ip_addr, "-n", "1"], stdout=subprocess.PIPE).communicate()[0]
        cut = out.split("=")[2]
        latency = cut.split("ms")[0]
        cut = out.split(" = ")[3]
        pl = cut.split("\n")[0]
        packet_loss = pl.replace("\n", "")


def int_to_text(i):
    if i == 0:
        return "Yes"
    else:
        return "No"


def diff(a, b):
    if b == -1:
        return 0

    if int(a) > int(b):
        return int(a) - int(b)
    else:
        return int(b) - int(a)
