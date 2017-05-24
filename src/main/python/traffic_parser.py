import re
import logging
import argparse
import sys
import os
import datetime
import time
import pytz
import json

"""
Parse daily tcpdump file, and create ImeiTracking object for the tracking information of the elapsed minute.
For each IMEI appends information to a daily file, and maintains a file with the last information.
"""


# Variables
_today = (datetime.datetime.now()).strftime('%Y-%m-%d')
log_path = '/data/IMEI_tracking/'
application_log_path = '/data/logs/'
tcpdump_log = '/data/incoming_traffic/incoming_traffic'
#regex getting relevant information from the tcpdump.
regex = re.compile('(\+\d+)(,)(GPRMC,)(\d+\.\d+),\w,(\d+\.\d+),\w,(\d+\.\d+)(.*)(\d{6})(.*)(imei:)(\d+)')


# Arguments parsing
parser = argparse.ArgumentParser(
    description='tcpdump traffic parser.'
)
parser.add_argument("-v", "--verbose", "--debug",
                    help="increase output verbosity", action="store_true"
                   )
args = parser.parse_args()



# Logger et handler
logger = logging.getLogger('traffic_parser')
logger.setLevel(logging.DEBUG)
fh = logging.FileHandler(application_log_path + 'traffic_parser_' + _today + '.log')
fh.setLevel(logging.INFO)
ch = logging.StreamHandler()
if args.verbose:
    ch.setLevel(logging.DEBUG)
    fh.setLevel(logging.DEBUG)
else:
    ch.setLevel(logging.INFO)
formatter = logging.Formatter(
    '%(asctime)s - %(name)s - %(levelname)s - %(message)s')
fh.setFormatter(formatter)
ch.setFormatter(formatter)
logger.addHandler(fh)
logger.addHandler(ch)



class ImeiTracking:

    """gathering info on IMEI tracking."""

    def __init__(self, imei, msisdn, timetracked, datetracked, latitude, longitude, ):
        self.imei = imei
        self.msisdn = msisdn
        self.timetracked = timetracked
        self.datetracked = datetracked
        self.latitude = latitude
        self.longitude = longitude

    def __repr__(self):
        return 'IMEI: {0}, msisdn: {1}, timetracked: {2}, datetracked: {3}, latitude: {4}, longitude: '\
        '{5}'.format(self.imei, self.msisdn, self.timetracked, self.datetracked, self.latitude, \
        self.longitude)


def fatal(msg):
    """Fonction erreur"""
    logger.error(msg)
    sys.exit(1)


def regex_on_tcpdump():

    """
    Parsing tcpdump and building ImeiTracking objects.
    time must match utctime of the elapsed minute.
    Returns ImeiTracking list.
    """

    IMEI_list = list()
    # UTC time minus 1 minute used for the regex on the tcpdump
    utctime = (datetime.datetime.utcnow()-datetime.timedelta(minutes=1)).strftime('%H%M')
    try:
        with open(tcpdump_log) as file:
            for line in file:
                bim = regex.search(line)
                if bim:
                    msisdn = bim.group(1)
                    timetracked = bim.group(4)
                    latitude = bim.group(5)
                    longitude = bim.group(6)
                    datetracked = bim.group(8)
                    imei = bim.group(11)
                    logger.debug('IMEI found in tcpdump : ' + str(imei) + ' with timetracked : ' +\
                    timetracked)
                    if timetracked[:4] == utctime:
                        try:
                            new_imei = ImeiTracking(imei, msisdn, timetracked, datetracked, latitude, longitude)
                            logger.info('IMEI tracked : ' + str(new_imei))
                            IMEI_list.append(new_imei)
                        except:
                            logger.error('Issue with the data extracted from the tcpdump, check the input file.')
    except:
        fatal('No tcpdump file available!')
    if len(IMEI_list) == 0:
        logger.info('no IMEI tracked during the elapsed minute.')
    return IMEI_list



def formating_results(ImeiTracking_obj):

    """
    Formats results and returns a dict to be dumped in json.
    For time we pick date and time from tcpdump and returns only time but in the correct 
    time zone (we get UTC from tcpdump).
    """

    input_dict = {}
    local_tz = pytz.timezone('Europe/Paris')
    input_dict['imei'] = ImeiTracking_obj.imei
    input_dict['msisdn'] = ImeiTracking_obj.msisdn
    time_from_tcpdump = ImeiTracking_obj.datetracked + ImeiTracking_obj.timetracked
    time_1 = datetime.datetime.strptime(time_from_tcpdump[:12], '%d%m%y%H%M%S')
    # change tz
    time_2 = time_1.replace(tzinfo=pytz.utc).astimezone(local_tz)
    # we keep only hour:minute:second
    time_3 = time_2.strftime('%H:%M:%S')
    input_dict['time'] = time_3
    input_dict['latitude'] = ImeiTracking_obj.latitude
    input_dict['longitude'] = ImeiTracking_obj.longitude
    return input_dict



def main():
    result_list = regex_on_tcpdump()
    for a in result_list:
        dailyoutput_file = os.path.join(log_path, _today + '_' + str(a.imei) + '.json')
        liveoutput_file = os.path.join(log_path, str(a.imei) + '.json')
        f1 = open(dailyoutput_file, 'a')
        f2 = open(liveoutput_file, 'w')
        towrite = json.dumps(formating_results(a))
        f1.write('%s \n' % towrite)
        f2.write('%s \n' % towrite)


main()
