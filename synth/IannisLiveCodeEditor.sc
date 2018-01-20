IannisLiveCodeEditor : IannisSynthMapParameter {
    *new {arg parent;
        ^super.new.initLiveCode(parent);
    }

    initLiveCode {arg parent;
        parentSynthPage = parent;
        key = \default;
        isOn = true;

        this.initProxies();
        this.initInnerViews();
        this.initLayout();
    }

    initProxies {
        proxiesGroup = this.getParentSynthController().node;

        // 127.do is faster than Array.fill
        proxies = [];
        127.do({
            var proxy = NodeProxy();
            proxy.group = proxiesGroup;
            proxies = proxies.add(proxy);
        });
    }

    initInnerViews {
        this.initEvaluateButton();
        this.initXFadeNumberBox();
        this.initXFadeLabel();
        this.initParametersView();
        this.initTextView();
        this.initEditButton();
    }

    initTextView {
        super.initTextView();
        textView.fixedHeight = 340;
        textView.onLoadFinished = {arg wv;
            wv.setValue(
                "/*\n"
                "<prow>\n"
                "Amp: amp\n"
                "*/\n"
                "\n"
                "var env = EnvGen.kr(Env.asr(0.01, 1, 2), gate: \\selfgate.kr);\n"
                "env * SinOsc.ar(\\selfnote.kr, 0, \\amp.kr*0.1)!2;"
            );
        };
    }

    initEditButton {
        super.initEditButton();
        editButton.valueAction = 1;
    }

    initLayout {
        this.layout = VLayout(
            HLayout(
                editButton, evaluateButton,
                nil,
                xFadeLabel, xFadeNumberBox
            ),
            parametersView,
            textView
        );
    }

    getParentSynthController {
        ^this.parentSynthPage;
    }

    evaluateCodeAction {arg code;
        var parentSynthController = this.getParentSynthController();
        var preset = parentSynthController
        .presetsManagerController
        .presetsManager
        .currentPreset;
        var compiled = code.compile();
        var rout = Routine({
            this.releaseAll();
            // update NodeProxy
            this.proxies[0].source = compiled;

            (this.proxies.size-2).do({arg n; 
                this.proxies[n+1] = this.proxies[0].copy;
                this.proxies[n+1].release();
            });

            Server.default.sync();

            // create UI
            this.parseCode(code);

            Server.default.sync();

            this.loadPresetDataToNode(preset);
        });

        AppClock.play(rout);
    }

    releaseAll {
        this.proxies.do({arg proxy; proxy.release()});
    }

    onNoteOn {arg noteNumber, velocity;
        var proxy = this.proxies[noteNumber];
        proxy.set(\selfnote, noteNumber.midicps);
        proxy.set(\selfvelocity, velocity.linlin(0, 127, 0, 1));
        proxy.set(\selfgate, 1);
        this.playProxyAtIndex(noteNumber);
    }

    playProxyAtIndex {arg index;
        var outputBus = this.getParentSynthController().outputBus.index;
        var numOfChannels = 2;
        var group = this.getParentSynthController().node;
        this.proxies[index].play(outputBus, numOfChannels, group);
    }

    onNoteOff {arg noteNumber;
        this.proxies[noteNumber].set(\selfgate, 0);
        this.proxies[noteNumber].set(\selfvelocity, 0);
    }

}

