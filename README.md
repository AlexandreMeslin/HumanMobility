# HumanMobility
## An human mobility simulator

[![MUSANet|Solid](https://raw.githubusercontent.com/meslin8752/InterSCity-onibus/master/PoweredByMUSANet.png)](https://musanet.meslin.com.br/)

This project simulates human mobility based on an actual trace. The humans take with them a simulated version of the Mobile Hub for Android.

## Running HumanMobility

Considering users at `dataset` directory, the following command line parameters are a suggestion for running HumanMobility:
```sh
-a 127.0.0.1 -p 5500 -d "/media/meslin/4E7E313D7E311EE1/Users/meslin/Google Drive/workspace-desktop-ubuntu/HumanMobility/dataset" -o -43.6 -v 10 -s 0.3 -l -23
```

That means:
- latitude -23
- longitude -43.6
- simulation speed 10x
- simulation scale 0.3x

Execute the class `br.com.meslin.humanMobility.main.HumanMobility` as Java application with the command line parameters describe previously.