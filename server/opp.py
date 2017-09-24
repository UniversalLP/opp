import calendar
import os.path

import time
import datetime

import ping

db_name = ""
db_folder = "./db"
db_file = file
config_path = "./pyopp.cfg"
target_address = "google.com"
interval = 1000
auto_save_interval = 600
ping_diff = 5
time_now = datetime.datetime.now()
ping_count = 0


def log(msg):
    global time_now
    time_now = datetime.datetime.now()
    print(datetime.datetime.now().strftime('[%d.%m.%Y %H:%M:%S] ') + msg)


# Platform independent way of getting time since 1970.1.1
def get_epoch_time():
    return calendar.timegm(datetime.datetime.now().timetuple())


def create_or_open_config():
    global target_address
    global interval
    global ping_diff
    global db_folder
    global auto_save_interval

    if os.path.isfile(config_path):
        log("Found config! Loading it now")

        cfg_file = open(config_path, "r")

        for line in cfg_file:
            if ~line.startswith("#"):
                if line.startswith("ip"):
                    target_address = line.split("=")[1].replace("\n", "")
                elif line.startswith("interval"):
                    interval = int(line.split("=")[1])
                elif line.startswith("ping_diff"):
                    ping_diff = int(line.split("=")[1])
                elif line.startswith("db_path"):
                    db_folder = line.split("=")[1].replace("\n", "")
                elif line.startswith("auto_save"):
                    auto_save_interval = int(line.split("=")[1])
    else:
        log("Config file doesn't exist! Creating under " + config_path)

        cfg_file = open(config_path, "w")

        cfg_file.write("# Pyopp config file v1.0\n")
        cfg_file.write("# IP to ping. (Default: " + target_address + ")\n")
        cfg_file.write("ip=" + target_address + "\n")
        cfg_file.write("# Ping interval in ms. (Default: " + str(interval) + ")\n")
        cfg_file.write("interval=" + str(interval) + "\n")
        cfg_file.write("# Difference in latency at which a Ping result will be written down. (Default: " + str(ping_diff) + ")\n")
        cfg_file.write("ping_diff=" + str(ping_diff) + "\n")
        cfg_file.write("# Path for database folder. (Default: " + db_folder + ")\n")
        cfg_file.write("db_path=" + db_folder + "\n")
        cfg_file.write("# Autosave database after x ping attemps to prevent dataloss. (Default: " + str(auto_save_interval) + ")\n")
        cfg_file.write("auto_save=" + str(auto_save_interval) + "\n")


def check_db_path():
    if not os.path.exists(db_folder):
        os.makedirs(db_folder)


def find_or_create_db_file():
    global db_file
    global db_name

    files = [f for f in os.listdir(db_folder) if os.path.isfile(os.path.join(db_folder, f))]
    today = datetime.datetime.now()
    flag = False

    for f in files:
        t = os.path.getmtime(db_folder + "/" + f)
        date = datetime.datetime.fromtimestamp(t)

        year = date.year
        month = date.month
        day = date.day

        if today.year == year and today.month == month and today.day == day:
            log("Found exisiting database for today! " + f)
            db_name = f
            db_file = open(db_folder + "/" + f, "a")
            flag = True
            break

    if flag:
        return

    db_name = "DB_" + datetime.datetime.now().strftime('%d-%m-%Y') + ".csv"
    log("Couldn't find database for today! Creating one now as " + db_name)
    db_file = open(db_folder + "/" + db_name, "a")


if not ping.can_run():
    log("pyopp can only handle output of windows and linux ping commmands.")
    log("Modify opp.py below this message and remove the exit() command to try it on other platforms")
    log("(No guarantee)")
    exit()


create_or_open_config()
check_db_path()
find_or_create_db_file()

log("Loaded config successfully!")
log(" Address   : " + target_address)
log(" Interval  : " + str(interval))
log(" Ping diff : " + str(ping_diff))

begin_time = get_epoch_time()
start = datetime.datetime.now()

valid_result = False

while True:
    try:
        time_now = datetime.datetime.now()
        if not time_now.day == start.day:
            log(" Day has changed to " + str(time_now.weekday()))
            log(" Creating new database for today...")
            find_or_create_db_file()
            start = datetime.datetime.now()

        ping.ping_ip(target_address)
        log("Ping results: " + str(ping.latency) + "ms, Packetloss: " + ping.int_to_text(ping.packet_loss))

        last_latency = int(ping.last_latency)
        latency = int(ping.latency)
        diff = int(ping_diff)

        valid_result = isinstance(last_latency, int) and isinstance(latency, int) \
            and isinstance(diff, int)

        print(isinstance(last_latency, int))
        print(isinstance(latency, int))
        print(isinstance(diff, int))

        if valid_result:
            if not ping.last_latency == -1 and ping.diff(ping.latency, ping.last_latency) >= ping_diff \
                    or ping.packet_loss == 1:
                log(" Latency fluctuation over " + str(ping_diff) + "!")
                db_file.write(str(begin_time) + ";")
                db_file.write(str(get_epoch_time()) + ";")
                db_file.write(str(ping.latency) + ";")
                db_file.write(str(ping.packet_loss))
                begin_time = get_epoch_time()

            ping_count += 1

            if ping_count >= auto_save_interval:
                log(" Autosaving...")
                db_file.close()
                db_file = open(db_folder + "/" + db_name, "a")
                ping_count = 0
        else:
            log("Invalid response from ping command! Skipping...")

        time.sleep(interval / 1000)
    except KeyboardInterrupt:
        log("Shutting down!")
        db_file.write(str(begin_time) + ";")
        db_file.write(str(get_epoch_time()) + ";")
        db_file.write(str(ping.latency) + ";")
        db_file.write(str(ping.packet_loss))
        db_file.close()
        exit()
