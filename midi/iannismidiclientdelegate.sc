+ IannisMIDIInManagerController {

  didDisableMIDIInput {

  }

  didUpdateMIDIChannel {arg newValue;

  }

  didSelectNewDevice {arg device;
    this.midiManager.map[\noteOn].free();
    this.midiManager.map[\noteOff].free();

    if (this.midiManager.midiInputEnabled) {
      var voices = nil!127;

      // NoteON
      this.midiManager.map[\noteOn] = MIDIFunc.noteOn({arg val, num, chan, src;
        if (this.midiManager.selectedDevice.uid == src) {
          if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
            var values = this.parentController.presetsManagerController.presetsManager.currentPreset.values.deepCopy;
            
            if (values[\bufnum].notNil) {
              values[\bufnum] = values[\bufnum][\value];
            };

            if (values[\env].notNil) {
              values[\env] = [
                values[\env][\env][\attack],
                values[\env][\env][\decay],
                values[\env][\env][\sustain],
                values[\env][\env][\release]
              ];
            };

            values[\freq] = num.midicps;
            values[\velocity] = val;
            
            voices[num] = Synth(
              this.parentController.synthDefName,
              values.getPairs, 
              this.parentController.node
            );
          }
        }
      });

      // NoteOFF
      this.midiManager.map[\noteOff] = MIDIFunc.noteOff({arg val, num, chan, src;
        if (this.midiManager.selectedDevice.uid == src) {
          if ((this.midiManager.channel == 0) || (this.midiManager.channel == (chan+1))) {
            voices[num].release();
          }
        }
      });

    }
  }

  didUpdateMIDISources {
    var devicesNames = IannisMIDIClient.sources.collect(_.name);
    AppClock.sched(0, {
      midiSourcesMenu.items = devicesNames.insert(0, "None");
    });
  }
}
