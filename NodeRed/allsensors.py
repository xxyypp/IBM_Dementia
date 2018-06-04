#!/usr/bin/python
#--------------------------------------
#    ___  ___  _ ____
#   / _ \/ _ \(_) __/__  __ __
#  / , _/ ___/ /\ \/ _ \/ // /
# /_/|_/_/  /_/___/ .__/\_, /
#                /_/   /___/
#
#           bme280.py
#  Read data from a digital pressure sensor.
#
#  Official datasheet available from :
#  https://www.bosch-sensortec.com/bst/products/all_products/bme280
#
# Author : Matt Hawkins
# Date   : 25/07/2016
#
# http://www.raspberrypi-spy.co.uk/
#
#--------------------------------------
import smbus
import time
from ctypes import c_short
from ctypes import c_byte
from ctypes import c_ubyte

DEVICE = 0x77 # Default device I2C address


bus = smbus.SMBus(1) # Rev 2 Pi, Pi 2 & Pi 3 uses bus 1
                     # Rev 1 Pi uses bus 0

def getShort(data, index):
  # return two bytes from data as a signed 16-bit value
  return c_short((data[index+1] << 8) + data[index]).value

def getUShort(data, index):
  # return two bytes from data as an unsigned 16-bit value
  return (data[index+1] << 8) + data[index]

def getChar(data,index):
  # return one byte from data as a signed char
  result = data[index]
  if result > 127:
    result -= 256
  return result

def getUChar(data,index):
  # return one byte from data as an unsigned char
  result =  data[index] & 0xFF
  return result

def readBME280ID(addr=DEVICE):
  # Chip ID Register Address
  REG_ID     = 0xD0
  (chip_id, chip_version) = bus.read_i2c_block_data(addr, REG_ID, 2)
  return (chip_id, chip_version)

def readBME280All(addr=DEVICE):
  # Register Addresses
  REG_DATA = 0xF7
  REG_CONTROL = 0xF4
  REG_CONFIG  = 0xF5

  REG_CONTROL_HUM = 0xF2
  REG_HUM_MSB = 0xFD
  REG_HUM_LSB = 0xFE

  # Oversample setting - page 27
  OVERSAMPLE_TEMP = 2
  OVERSAMPLE_PRES = 2
  MODE = 1

  # Oversample setting for humidity register - page 26
  OVERSAMPLE_HUM = 2
  bus.write_byte_data(addr, REG_CONTROL_HUM, OVERSAMPLE_HUM)

  control = OVERSAMPLE_TEMP<<5 | OVERSAMPLE_PRES<<2 | MODE
  bus.write_byte_data(addr, REG_CONTROL, control)

  # Read blocks of calibration data from EEPROM
  # See Page 22 data sheet
  cal1 = bus.read_i2c_block_data(addr, 0x88, 24)
  cal2 = bus.read_i2c_block_data(addr, 0xA1, 1)
  cal3 = bus.read_i2c_block_data(addr, 0xE1, 7)

  # Convert byte data to word values
  dig_T1 = getUShort(cal1, 0)
  dig_T2 = getShort(cal1, 2)
  dig_T3 = getShort(cal1, 4)

  dig_P1 = getUShort(cal1, 6)
  dig_P2 = getShort(cal1, 8)
  dig_P3 = getShort(cal1, 10)
  dig_P4 = getShort(cal1, 12)
  dig_P5 = getShort(cal1, 14)
  dig_P6 = getShort(cal1, 16)
  dig_P7 = getShort(cal1, 18)
  dig_P8 = getShort(cal1, 20)
  dig_P9 = getShort(cal1, 22)

  dig_H1 = getUChar(cal2, 0)
  dig_H2 = getShort(cal3, 0)
  dig_H3 = getUChar(cal3, 2)

  dig_H4 = getChar(cal3, 3)
  dig_H4 = (dig_H4 << 24) >> 20
  dig_H4 = dig_H4 | (getChar(cal3, 4) & 0x0F)

  dig_H5 = getChar(cal3, 5)
  dig_H5 = (dig_H5 << 24) >> 20
  dig_H5 = dig_H5 | (getUChar(cal3, 4) >> 4 & 0x0F)

  dig_H6 = getChar(cal3, 6)

  # Wait in ms (Datasheet Appendix B: Measurement time and current calculation)
  wait_time = 1.25 + (2.3 * OVERSAMPLE_TEMP) + ((2.3 * OVERSAMPLE_PRES) + 0.575) + ((2.3 * OVERSAMPLE_HUM)+0.575)
  time.sleep(wait_time/1000)  # Wait the required time  

  # Read temperature/pressure/humidity
  data = bus.read_i2c_block_data(addr, REG_DATA, 8)
  pres_raw = (data[0] << 12) | (data[1] << 4) | (data[2] >> 4)
  temp_raw = (data[3] << 12) | (data[4] << 4) | (data[5] >> 4)
  hum_raw = (data[6] << 8) | data[7]

  #Refine temperature
  var1 = ((((temp_raw>>3)-(dig_T1<<1)))*(dig_T2)) >> 11
  var2 = (((((temp_raw>>4) - (dig_T1)) * ((temp_raw>>4) - (dig_T1))) >> 12) * (dig_T3)) >> 14
  t_fine = var1+var2
  temperature = float(((t_fine * 5) + 128) >> 8);

  # Refine pressure and adjust for temperature
  var1 = t_fine / 2.0 - 64000.0
  var2 = var1 * var1 * dig_P6 / 32768.0
  var2 = var2 + var1 * dig_P5 * 2.0
  var2 = var2 / 4.0 + dig_P4 * 65536.0
  var1 = (dig_P3 * var1 * var1 / 524288.0 + dig_P2 * var1) / 524288.0
  var1 = (1.0 + var1 / 32768.0) * dig_P1
  if var1 == 0:
    pressure=0
  else:
    pressure = 1048576.0 - pres_raw
    pressure = ((pressure - var2 / 4096.0) * 6250.0) / var1
    var1 = dig_P9 * pressure * pressure / 2147483648.0
    var2 = pressure * dig_P8 / 32768.0
    pressure = pressure + (var1 + var2 + dig_P7) / 16.0

  # Refine humidity
  humidity = t_fine - 76800.0
  humidity = (hum_raw - (dig_H4 * 64.0 + dig_H5 / 16384.0 * humidity)) * (dig_H2 / 65536.0 * (1.0 + dig_H6 / 67108864.0 * humidity * (1.0 + dig_H3 / 67108864.0 * humidity)))
  humidity = humidity * (1.0 - dig_H1 * humidity / 524288.0)
  if humidity > 100:
    humidity = 100
  elif humidity < 0:
    humidity = 0

  return temperature/100.0,pressure/100.0,humidity

def main():
    
   # while True:
      (chip_id, chip_version) = readBME280ID()
      #print "Chip ID     :", chip_id
      #print "Version     :", chip_version

      temperature,pressure,humidity = readBME280All()

      #print "Temperature : ", temperature, "C"
      #print "Pressure : ", pressure, "hPa"
      #print "Humidity : ", humidity, "%"
      
      print temperature
      print humidity


# Simple heart beat reader for Raspberry pi using ADS1x15 family of ADCs and a pulse sensor - http://pulsesensor.com/.
# The code borrows heavily from Tony DiCola's examples of using ADS1x15 with 
# Raspberry pi and WorldFamousElectronics's code for PulseSensor_Amped_Arduino

# Author: Udayan Kumar
# License: Public Domain

import time
# Import the ADS1x15 module.
import Adafruit_ADS1x15


if __name__ == '__main__':

    adc = Adafruit_ADS1x15.ADS1015()
    # initialization 
    GAIN = 2/3  
    curState = 0
    thresh = 525  # mid point in the waveform
    P = 512
    T = 512
    stateChanged = 0
    sampleCounter = 0
    lastBeatTime = 0
    firstBeat = True
    secondBeat = False
    Pulse = False
    IBI = 600
    rate = [0]*10
    amp = 100
    end = False
    #timer = 1000
    lastTime = int(time.time()*1000)
    avpulse = []
    average = 0

    # Main loop. use Ctrl-c to stop the code
    while (sampleCounter < 10000):
            # read from the ADC
            Signal = adc.read_adc(0, gain=GAIN)   #TODO: Select the correct ADC channel. I have selected A0 here
            curTime = int(time.time()*1000)

            sampleCounter += curTime - lastTime;      #                   # keep track of the time in mS with this variable
            lastTime = curTime
            N = sampleCounter - lastBeatTime;     #  # monitor the time since the last beat to avoid noise
            #print N, Signal, curTime, sampleCounter, lastBeatTime

            ##  find the peak and trough of the pulse wave
            if Signal < thresh and N > (IBI/5.0)*3.0 :  #       # avoid dichrotic noise by waiting 3/5 of last IBI
                if Signal < T :                        # T is the trough
                  T = Signal;                         # keep track of lowest point in pulse wave 

            if Signal > thresh and  Signal > P:           # thresh condition helps avoid noise
                P = Signal;                             # P is the peak
                                                    # keep track of highest point in pulse wave

              #  NOW IT'S TIME TO LOOK FOR THE HEART BEAT
              # signal surges up in value every time there is a pulse
            if N > 250 :                                   # avoid high frequency noise
                if  (Signal > thresh) and  (Pulse == False) and  (N > (IBI/5.0)*3.0)  :       
                  Pulse = True;                               # set the Pulse flag when we think there is a pulse
                  IBI = sampleCounter - lastBeatTime;         # measure time between beats in mS
                  lastBeatTime = sampleCounter;               # keep track of time for next pulse

                  if secondBeat :                        # if this is the second beat, if secondBeat == TRUE
                    secondBeat = False;                  # clear secondBeat flag
                    for i in range(0,10):             # seed the running total to get a realisitic BPM at startup
                      rate[i] = IBI;                      

                  if firstBeat :                        # if it's the first time we found a beat, if firstBeat == TRUE
                    firstBeat = False;                   # clear firstBeat flag
                    secondBeat = True;                   # set the second beat flag
                    continue                              # IBI value is unreliable so discard it


                  # keep a running total of the last 10 IBI values
                  runningTotal = 0;                  # clear the runningTotal variable    

                  for i in range(0,9):                # shift data in the rate array
                    rate[i] = rate[i+1];                  # and drop the oldest IBI value 
                    runningTotal += rate[i];              # add up the 9 oldest IBI values

                  rate[9] = IBI;                          # add the latest IBI to the rate array
                  runningTotal += rate[9];                # add the latest IBI to runningTotal
                  runningTotal /= 10;                     # average the last 10 IBI values 
                  BPM = 60000/runningTotal;               # how many beats can fit into a minute? that's BPM!
                  if sampleCounter > 5000 :
                      avpulse.append(BPM);
                      #print 'BPM: {}'.format(BPM)
                  #end = True

            if Signal < thresh and Pulse == True :   # when the values are going down, the beat is over
                Pulse = False;                         # reset the Pulse flag so we can do it again
                amp = P - T;                           # get amplitude of the pulse wave
                thresh = amp/2 + T;                    # set thresh at 50% of the amplitude
                P = thresh;                            # reset these for next time
                T = thresh;

            if N > 2500 :                          # if 2.5 seconds go by without a beat
                thresh = 512;                          # set thresh default
                P = 512;                               # set P default
                T = 512;                               # set T default
                lastBeatTime = sampleCounter;          # bring the lastBeatTime up to date        
                firstBeat = True;                      # set these to avoid noise
                secondBeat = False;                    # when we get the heartbeat back
                #print "no beats found"
               # end = True

            time.sleep(0.005)
            #timer -=0.02# Simple heart beat reader for Raspberry pi using ADS1x15 family of ADCs and a pulse sensor - http://pulsesensor.com/.
    if avpulse:
        for i in range (0,len(avpulse)):
            average += avpulse[i];
               
        average = average/len(avpulse);
    
        print '{}'.format(average)
    else:
        print 'no beats found'
            
            
    main()
    time.sleep(0.5)

