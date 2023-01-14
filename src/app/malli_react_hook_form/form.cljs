(ns app.malli-react-hook-form.form
  (:require
    [clojure.string :as str]
    [helix.core :as h :refer [<> $ fnc]]
    [helix.dom :as d]
    [goog.object :as g]
    ["react-hook-form" :refer [useForm useFormContext useController FormProvider]]
    ["react" :as react]))

(def base-input
  (react/forwardRef
   (fnc base-input [{:keys [label error] :as props} input-ref]
        (let [input-props (dissoc props :label :error)]
          (d/div
           (d/div
            (when label (d/label label))
            (d/input {:& input-props :ref input-ref}))
           (when error (d/div {:style {:marginTop "10px" :color "hsl(341deg 94% 43%)"}} error)))))))

;; using forwardRef we can still use react refs from the calling code if we wish

"
Here we use `useController` which allows using controlled inputs like you might find in a UI component library.
This code will work as is with something like material design components or antd - just replace base-input with
the component from your favorite library.
"
(def ui-form-input
  (react/forwardRef
   (fnc base-input
        [{:keys [default-value on-change on-change-xf] :as props
          :or   {on-change identity
                 on-change-xf identity}}
         input-ref]
        (let [default-value (or default-value "")
              form-context  (useFormContext)]
          (assert form-context "Missing form context for ui-form-input")
          (assert (:name props) "Missing 'name' context for ui-form-input")

          (let [input-props (dissoc props :default-value :on-change :on-change-xf)
                control     (.-control form-context)
                controller  (useController #js{:name         (:name props)
                                               :control      control
                                               :rules        (:rules props)
                                               :defaultValue default-value})
                field       (.-field controller)
                field-state (.-fieldState controller)
                invalid?    (g/get field-state "invalid")
                error-msg   (and invalid? (g/getValueByKeys field-state "error" "message"))]
            (d/div
             (d/pre "touched: " (pr-str (g/get field-state "isTouched")))
             (d/pre "dirty: " (pr-str (g/get field-state "isDirty")))
             (d/pre "invalid: " (pr-str (g/get field-state "invalid")))
             ($ base-input
                {:&         input-props
                 :error     error-msg
                 :on-change (fn [e]
                              (let [value (on-change-xf e)]
                                (on-change value)
                            ;; this also works, you can for example parse the string to send to react hook form
                            ;; for date or number input etc.
                            ;(.onChange field (.. e -target -value))
                                (.onChange field value)))
                 :on-blur   (.-onBlur field)
                 :value     (.-value field)
                 :ref       (or input-ref (.-ref field))})))))))

(defn filter-telephone-num [e]
  (if (empty? (-> e .-target .-value))
    ""
    (str/join (filter (into #{\( \) \- \space} (map str (range 10))) (-> e .-target .-value)))))

(def ui-telephone-input
  (react/forwardRef
   (fnc [args]
        ($ ui-form-input {:& args :type "tel" :on-change-xf filter-telephone-num}))))
