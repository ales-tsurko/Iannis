IannisLiveCodeEditor : IannisSynthMapParameter {
    *new {arg parent;
        ^super.new.initLiveCode(parent);
    }

    initLiveCode {arg parent;
        parentSynthPage = parent;
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
            proxies = proxies.add(proxy);
            proxy.group = proxiesGroup;
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

}
