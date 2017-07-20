+ IannisSynthMIDIViewController {

  initNoteOnMIDIFunc {
    this.midiManager.map[\noteOn] = MIDIFunc.noteOn({arg val, num, chan, src;
      if (this.midiManager.selectedDevice.uid == src) {
        if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
          if (this.parentSynthController.type == \synth) {
            var values = this.parentSynthController.node.getState.getPairs;
            var newVoice;

            values = values.addAll([\freq, num.midicps, \velocity, val]);

            // add it to the voices array
            this.midiManager.voicesManager.noteOn(
              num,
              this.parentSynthController.synthDefName,
              values,
              this.parentSynthController.node
            );

          };

          // call map parameter bindings
          this.parentSynthController.mapView.parameters.do({arg map;
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
          if (this.parentSynthController.type == \synth) {
            this.midiManager.voicesManager.noteOff(num);
          };
          // call map parameter bindings
          this.parentSynthController.mapView.parameters.do({arg map;
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
          if (this.parentSynthController.type == \synth) {
            this.midiManager.voicesManager.sustainPedalIsOn = (val == 127);
          };
        }
      };
    }, 64);

    this.midiManager.map[\sustainPedal].permanent = true;
  }

  initPitchBendMIDIFunc {
    this.midiManager.map[\bend] = MIDIFunc.bend({arg val, chan, src;
      if (this.midiManager.selectedDevice.uid == src) {
        if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
          if (this.parentSynthController.type == \synth) {
            var value = (val>>7)&0xFF;
            this.midiManager.voicesManager.pitchBend(value);
          };
        }
      };
    });

    this.midiManager.map[\bend].permanent = true;
  }

  didDisableMIDIInput {
    this.midiManager.map[\noteOn].free();
    this.midiManager.map[\noteOff].free();
    this.midiManager.map[\sustainPedal].free();
    this.midiManager.map[\bend].free();
  }

  didEnableMIDIInput {
    // NoteON
    this.initNoteOnMIDIFunc();

    // NoteOFF
    this.initNoteOffMIDIFunc();

    // Sustain Pedal
    this.initSustainPedalMIDIFunc();

    // Pitch Bend
    this.initPitchBendMIDIFunc();
  }

  didUpdateMIDIChannel {arg newValue;

  }

  didSelectNewDevice {arg device;
    this.didDisableMIDIInput();

    if (this.midiManager.midiInputEnabled) {
      this.didEnableMIDIInput();
    }
  }

  didUpdateMIDISources {
    var devicesNames = IannisMIDIClient.sources.collect(_.name);
    devicesNames = devicesNames.insert(0, "None");

    // keyboard
    midiSourcesMenu.items = devicesNames;

    // parameters
    parameters.do({arg view;
      view.sourcesPopup.items = devicesNames;
      // self assign to fire an event
      view.sourceUID = view.sourceUID; 
    });

    // autoselect previously disconnected device or select None
    // on diconnection
    this.midiManager!?{
      midiSourcesMenu.valueAction = this.midiManager.selectedDeviceIndex;
    };
  }

  didAddMIDIControllerToMap {arg key, sourceUID, ccNum, channel;
    this.addParameter(key, sourceUID, ccNum, channel);

    // update preset
    this.parentSynthController
    .presetsManagerController
    .presetsManager
    .currentPreset
    .addMIDIBinding(key, sourceUID, ccNum, channel);
  }

  didRemoveMIDIControllerFromMap {arg key;
    // update preset
    this.parentSynthController
    .presetsManagerController
    .presetsManager
    .currentPreset
    .removeMIDIBinding(key);
  }

  didUpdateMIDIControllerInMap {arg key, sourceUID, ccNum, channel;
    parameters[key]!?{
      sourceUID!?{
        parameters[key].sourceUID = sourceUID;
      };

      ccNum!?{
        parameters[key].ccNum = ccNum;
      };

      channel!?{
        parameters[key].channel = channel;
      };
    };

    // update preset
    this.parentSynthController
    .presetsManagerController
    .presetsManager
    .currentPreset
    .updateMIDIBinding(key, sourceUID, ccNum, channel);
  }
}
