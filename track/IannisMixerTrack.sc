IannisMixerTrack {
    var <node,
    <>name,
    <bus,
    <innerBus,
    <isSolo = false,
    <isMute = false,
    <instrumentsManager,
    <effectsManager,
    <gain = 0,
    <pan = 0,
    <isMaster;

    *new {arg name, isMaster, innerBus;
        ^super.new.init(name, isMaster, innerBus);
    }

    init {arg aName, isMas, ibus;
        isMaster = isMas;
        name = aName?"New Track";
        bus = Bus.control(Server.default, 4);
        innerBus = ibus;
        // postln(innerBus);

        if(isMaster){
            node = Synth(
                "by.alestsurko.iannis.track-master.controller",
                [\gain, 0, \pan, 0, \inbus, 0, \levelbus, bus]
            );
        }{
            node = Synth(
                "by.alestsurko.iannis.track.controller",
                [\gain, 0, \pan, 0, \inbus, innerBus, \outputbus, 0, \levelbus, bus]
            );
        };


        if(isMaster.not){instrumentsManager = IannisInstrumentsManager(this)};
        effectsManager = IannisEffectsManager(this);

        effectsManager.group.moveBefore(this.node);
    }

    cleanUp {
        if(isMaster.not){instrumentsManager.cleanUp()};
        node.free();
    }

    gain_ {arg newValue;
        gain = newValue;

        if (isMute) {
            node.set(\gain, -inf);
        } {
            node.set(\gain, gain);
        };
    }

    pan_ {arg newValue;
        pan = newValue;
        node.set(\pan, pan);
    }

    isMute_ {arg newValue;
        var gainVal = this.gain;
        isMute = newValue;

        // trigger gain
        this.gain = gainVal;
    }

    isSolo_ {arg newValue;
        isSolo = newValue;
    }
}

// Instruments manager delegate
+ IannisMixerTrack {
    didSelectInstrument {arg instrumentDesc, synthViewController;
        this.effectsManager.group.moveAfter(synthViewController.node);
        node.moveAfter(this.effectsManager.group);
    }
}

// Effects manager delegate
+ IannisMixerTrack {
    didAddEffect {arg effect;
    }

    willRemoveEffectAtIndex {arg index, effectViewController;
    }

    didChangeEffectAtIndex {arg index, effectViewController;
    }

    didMoveEffectToIndex {arg fromIndex, toIndex;
    }
}
