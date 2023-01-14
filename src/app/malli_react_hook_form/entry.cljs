(ns app.malli-react-hook-form.entry
  (:require
    [cljs.pprint :refer [pprint]]
    [helix.core :as h :refer [defnc $]]
    [helix.hooks :as hooks]
    [app.malli-react-hook-form.form :as form]
    [helix.dom :as d]
    [malli.core :as m]
    [goog.object :as g]
    [malli.error :as me]
    ["react" :as react]
    ["react-hook-form" :refer [useForm FormProvider]]
    ["react-dom/client" :as react-dom]))

(def form-schema
  [:map
   [:username {:ui-form/label "Username"
               :ui-form/component form/ui-form-input}
    [:string {:min 4 :max 15}]]
   [:telephone {:ui-form/label "Telephone num"
                :ui-form/placeholder "(913)-345-2303"
                :ui-form/component form/ui-telephone-input}
    [:re {:error/message "Please enter a valid phone number"}
     #"^(?:\(\d{3}\)|\d{3})[- ]?\d{3}[- ]?\d{4}"]]])

(defn map-schema->inputs [schema on-change]
  (->>
   (m/children schema)
   (map (fn [[k opts]]
          (let [{:ui-form/keys [placeholder label component]
                 :or {placeholder "" label ""
                      component form/ui-form-input}} opts]
            (d/div {:key (name k)}
                   ($ component {:placeholder placeholder
                                 :label       label
                                 :on-change  #(on-change k %)
                                 :name        (name k)})))))))

(defn use-malli-resolver [schema]
  (let [validator (m/validator schema)
        explain   (m/explainer schema)]
    (hooks/use-callback [schema]
      (fn [data-js]
        (let [data   (js->clj data-js :keywordize-keys true)
              valid? (validator data)]
          (if valid?
            #js{:values data :errors #js{}}
            (let [errs (reduce-kv
                        (fn [errs field messages]
                          (doto errs
                            (g/set (name field)
                              #js{:type "validation" :message (first messages)})))
                        #js{}
                        (me/humanize (explain data)))]
              #js{:values #js{} :errors errs})))))))

(defnc app []
  (let [resolver      (use-malli-resolver form-schema)
        form-methods  (useForm #js{:resolver resolver})
        handle-submit (.-handleSubmit form-methods)
        on-change     (hooks/use-callback [] (fn [k v] (.log js/console "CHANGE: " k ", " v)))
        on-submit     (hooks/use-callback [] (fn [data] (.log js/console "SUBMIT: " data)))]
    ($ FormProvider {:& form-methods}
       (d/div
        (d/pre (with-out-str (pprint form-schema)))
        (d/form {:onSubmit (handle-submit on-submit)}
          (d/div {:style
                  {:width           "40%" :display "flex" :margin-bottom "20px"
                    :justify-content "space-between"
                    :padding         "20px" :border "1px solid"}}
                  (map-schema->inputs form-schema on-change)
                  ;; alternatively, the fields can be defined statically:
                  ;; (d/div
                  ;;  ($ form/ui-form-input {:placeholder ""
                  ;;                         :label       "username"
                  ;;                         :name        "username"}))

                  ;; (d/div
                  ;;  ($ form/ui-form-input {:placeholder "(913)-345-2303"
                  ;;                         :label       "Telephone num"
                  ;;                         :type "tel"
                  ;;                         :name        "telephone"}))
                  )

          (d/button {:type "submit" :style {:padding    "10px 20px"
                                            :color      "oldLace"
                                            :background "hsl(300deg 60% 60%)"}} "Save")

          (d/button {:style    {:padding "10px 20px" :margin-left "60px"}
                      :on-click (fn [e]
                                  (.preventDefault e)
                                  (.reset form-methods))} "Reset"))))))

(defonce root (react-dom/createRoot (js/document.getElementById "app")))

(defn ^:export init []
  (.render root ($ react/StrictMode ($ app))))

(defn ^:dev/after-load refresh [] (.render root ($ react/StrictMode ($ app))))
