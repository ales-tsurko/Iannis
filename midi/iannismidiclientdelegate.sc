+ IannisMIDIInManagerController {

  didDisableMIDIInput {

  }

  didUpdateMIDIChannel {arg newValue;

  }

  didSelectNewDevice {arg device;
    this.midiManager.map[\noteOn].free();
    this.midiManager.map[\noteOff].free();

    if (this.midiManager.midiInputEnabled) {
      this.parentController.node.midiVoices??{this.parentController.node.midiVoices = nil!127};
      
      // NoteON
      this.midiManager.map[\noteOn] = MIDIFunc.noteOn({arg val, num, chan, src;
        if (this.midiManager.selectedDevice.uid == src) {
          if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
            var values = this.parentController.node.getState.getPairs;
            
            values = values.addAll([\freq, num.midicps, \velocity, val]);

            this.parentController.node.midiVoices[num] = Synth(
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

      // NoteOFF
      this.midiManager.map[\noteOff] = MIDIFunc.noteOff({arg val, num, chan, src;
        if (this.midiManager.selectedDevice.uid == src) {
          if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
            this.parentController.node.midiVoices[num].release();
            // call map parameter bindings
            this.parentController.mapView.parametersMaps.do({arg map;
              map.onNoteOff(num);
            });
          }
        }
      });

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
}
