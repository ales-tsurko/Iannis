IannisAceWrapper : WebView {
  var currentCode, condition;

  *new {
    ^super.new.init()
  }

  init {
    condition = Condition();

    this.url = "index.html".resolveRelative;

    this.onJavaScriptMsg = {arg view, msg;
      this.parseConsoleMessage(msg);
    };
  }

  parseConsoleMessage {arg msg;
    case 
    // evaluate
    {msg.contains("<-!code_evaluation_triggered!->")} {
      var code = msg.replace("<-!code_evaluation_triggered!->");
      code.interpretPrint;
      AppClock.sched(0, {condition.unhang()});
    }
    // get value
    {msg.contains("<-!get_value_triggered!->")} {
      var code = msg.replace("<-!get_value_triggered!->");
      currentCode = code;
      AppClock.sched(0, {condition.unhang()});
    }
    // else
    {true} {
      ("Ace log:"+msg).postln;
    };
  }

  setValue {arg newValue;
    this.evaluateJavaScript("editor.setValue(\""++newValue++"\")");
  }

  // update the currentCode, then calls the passed
  // callback (if any) with the currentCode as argument
  getValue {arg callback;
    AppClock.play(
      Routine({
        this.evaluateJavaScript("console.log(\"<-!get_value_triggered!->\"+editor.getValue())");
        condition.hang();
        callback!?{callback.value(currentCode)};
      });
    );
  }

  // evaluates entire document, then calls the callback (if any)
  evaluate {arg callback;
    AppClock.play(
      Routine({
        this.evaluateJavaScript("console.log(\"<-!code_evaluation_triggered!->\"+editor.getValue())");
        condition.hang();
        callback!?{callback.value()};
      });
    );
  }

  // evaluates selection or current line, then calls the callback (if any)
  evaluateSelection {arg callback;
    AppClock.play(
      Routine({
        var jsString = "var selectedText = editor.session.getTextRange(editor.getSelectionRange());
        if (selectedText.length > 0) {
          console.log(\"<-!code_evaluation_triggered!->\"+selectedText);
        } else {
          var currentLineNumber = editor.selection.getCursor().row;
          var currentLineText = editor.session.getLine(currentLineNumber);
          // return current line
          console.log(\"<-!code_evaluation_triggered!->\"+currentLineText);
        }";

        this.evaluateJavaScript(jsString);

        condition.hang();
        callback!?{callback.value()};
      });
    );
  }
}
