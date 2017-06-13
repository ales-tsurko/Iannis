+ IannisSynthMIDIViewController {

  didDisableMIDIInput {

  }

  didUpdateMIDIChannel {arg newValue;

  }

  didSelectNewDevice {arg device;
    this.midiManager.map[\noteOn].free();
    this.midiManager.map[\noteOff].free();
    this.midiManager.map[\sustainPedal].free();
    this.midiManager.map[\bend].free();

    if (this.midiManager.midiInputEnabled) {
      // NoteON
      this.initNoteOnMIDIFunc();

      // NoteOFF
      this.initNoteOffMIDIFunc();

      // Sustain Pedal
      this.initSustainPedalMIDIFunc();

      // Pitch Bend
      this.initPitchBendMIDIFunc();
    }
  }

  didUpdateMIDISources {
    var devicesNames = IannisMIDIClient.sources.collect(_.name);
    midiSourcesMenu.items = devicesNames.insert(0, "None");

    // autoselect previously disconnected device or select None
    // on diconnection
    this.midiManager!?{
      midiSourcesMenu.valueAction = this.midiManager.selectedDeviceIndex;
    };
  }

  initNoteOnMIDIFunc {
    this.midiManager.map[\noteOn] = MIDIFunc.noteOn({arg val, num, chan, src;
      if (this.midiManager.selectedDevice.uid == src) {
        if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
          var values = this.parentController.node.getState.getPairs;
          var newVoice;

          values = values.addAll([\freq, num.midicps, \velocity, val]);

          // add it to the voices array
          this.midiManager.voicesManager.noteOn(
            num,
            this.parentController.synthDefName,
            values,
            this.parentController.node
          );

          // call map parameter bindings
          this.parentController.mapView.parametersMaps.do({arg map;
            map.onNoteOn(num, val);
          });
        }
      }
    });

    // ignore CmdPeriod
    this.midiManager.map[\noteOn].permanent = true;
  }

  initNoteOffMIDIFunc {
    this.midiManager.map[\noteOff] = MIDIFunc.noteOff({arg val, num, chan, src;
      if (this.midiManager.selectedDevice.uid == src) {
        if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
          this.midiManager.voicesManager.noteOff(num);
          // call map parameter bindings
          this.parentController.mapView.parametersMaps.do({arg map;
            map.onNoteOff(num);
          });
        }
      }
    });

    // ignore CmdPeriod
    this.midiManager.map[\noteOff].permanent = true;
  }

  initSustainPedalMIDIFunc {
    this.midiManager.map[\sustainPedal] = MIDIFunc.cc({arg val, num, chan, src;

      if (this.midiManager.selectedDevice.uid == src) {
        if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
          this.midiManager.voicesManager.sustainPedalIsOn = (val == 127);
        }
      };
    }, 64);

    this.midiManager.map[\sustainPedal].permanent = true;
  }

  initPitchBendMIDIFunc {
    this.midiManager.map[\bend] = MIDIFunc.bend({arg val, chan, src;
      if (this.midiManager.selectedDevice.uid == src) {
        if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
          var value = (val>>7)&0xFF;
          this.midiManager.voicesManager.pitchBend(value);
        }
      };
    });

    this.midiManager.map[\bend].permanent = true;
  }

  didAddMIDIControllerToMap {arg key, sourceUID, ccNum, channel;
    this.addParameter(key, sourceUID, ccNum, channel);
  }

  didRemoveMIDIControllerFromMap {arg key;
  }

  didUpdateMIDIControllerInMap {arg key, sourceUID, ccNum, channel;
    sourceUID!?{
      parameters[key].sourceUID = sourceUID;
    };

    ccNum!?{
      parameters[key].ccNum = ccNum;
    };

    channel!?{
      parameters[key].channel = channel;
    };
  }
}
