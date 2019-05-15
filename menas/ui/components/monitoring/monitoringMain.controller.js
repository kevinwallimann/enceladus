 /*
 * Copyright 2018-2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
jQuery.sap.require("sap.m.MessageBox");
jQuery.sap.require("sap.ui.unified.DateRange");
jQuery.sap.require("sap.ui.core.mvc.Controller");

sap.ui.controller("components.monitoring.monitoringMain", {

  oFormatYyyymmdd: null,

  /**
   * Called when a controller is instantiated and its View controls (if
   * available) are already created. Can be used to modify the View before it
   * is displayed, to bind event handlers and do other one-time
   * initialization.
   *
   * @memberOf components.monitoring.monitoringMain
   */
  onInit: function () {
    this._model = sap.ui.getCore().getModel();
    this._router = sap.ui.core.UIComponent.getRouterFor(this);
    this._router.getRoute("monitoring").attachMatched(function (oEvent) {
      let arguments = oEvent.getParameter("arguments");
      this.routeMatched(arguments);
    }, this);
    this.oFormatYyyymmdd = sap.ui.core.format.DateFormat.getInstance({pattern: "yyyy-MM-dd", calendarType: sap.ui.core.CalendarType.Gregorian});
    this.setDefaultDateInterval()
    var oView = this.getView();
    oView.byId("multiheader-info").setHeaderSpan([2,1]);
    oView.byId("multiheader-checkpoint").setHeaderSpan([3,1]);
    oView.byId("multiheader-recordcount-raw").setHeaderSpan([5,1,1]);
    oView.byId("multiheader-recordcount-std").setHeaderSpan([0,2,1]);
    oView.byId("multiheader-recordcount-cnfrm").setHeaderSpan([0,2,1]);



  },

  _formFragments: {},

  getFormFragment: function (sFragmentName) {
    let oFormFragment = this._formFragments[sFragmentName];

    if (oFormFragment) {
      return oFormFragment;
    }
    const sFragmentId = this.getView().getId() + "--" + sFragmentName;
    oFormFragment = sap.ui.xmlfragment(sFragmentId, "components.dataset.conformanceRule." + sFragmentName + ".add", this);

    if (sFragmentName === "ConcatenationConformanceRule") {
      sap.ui.getCore().byId(sFragmentId + "--addInputColumn").attachPress(this.addInputColumn, this);
    }

    if (sFragmentName === "MappingConformanceRule") {
      sap.ui.getCore().byId(sFragmentId + "--mappingTableNameSelect").attachChange(this.mappingTableSelect, this);
      sap.ui.getCore().byId(sFragmentId + "--addJoinCondition").attachPress(this.addJoinCondition, this);
    }

    this._formFragments[sFragmentName] = oFormFragment;
    return this._formFragments[sFragmentName];
  },


  // remains
  datasetSelected: function (oEv) {
    let selected = oEv.getParameter("listItem").data("id");
    this._router.navTo("monitoring", {
      id: selected
    });
  },

  // remains
  routeMatched: function (oParams) {
    if (Prop.get(oParams, "id") === undefined) {
      MonitoringService.getDatasetList(true);
    } else {
      MonitoringService.getDatasetList();

      let sStartDate = "2018-06-01"
      let sEndDate = "2019-06-30"

      this._model.setProperty("/datasetId", oParams.id)
      MonitoringService.getMonitoringPoints(oParams.id);

      //MonitoringService.getDatasetCheckpoints(oParams.id);

    }
  },

  // Calendar

  handleCalendarSelect: function(oEvent) {
    var oCalendar = oEvent.getSource();
    this._updateText(oCalendar.getSelectedDates()[0]);
  },

  _updateText: function(oSelectedDates) {
    var oSelectedDateFrom = this.byId("selectedDateFrom");
    var oSelectedDateTo = this.byId("selectedDateTo");
    var oDate;
    if (oSelectedDates) {
      oDate = oSelectedDates.getStartDate();
      if (oDate) {
        oSelectedDateFrom.setText(this.oFormatYyyymmdd.format(oDate));
        this._model.setProperty("/dateFrom", this.oFormatYyyymmdd.format(oDate))
      } else {
        oSelectedDateTo.setText("No Date Selected");
        this._model.setProperty("/plotData", [])
        this._model.setProperty("/monitoringPoints", [])
      }
      oDate = oSelectedDates.getEndDate();
      if (oDate) {
        oSelectedDateTo.setText(this.oFormatYyyymmdd.format(oDate));
        this._model.setProperty("/dateTo", this.oFormatYyyymmdd.format(oDate))
      } else {
        oSelectedDateTo.setText("No Date Selected");
        this._model.setProperty("/plotData", [])
        this._model.setProperty("/monitoringPoints", [])
      }
    } else {
      oSelectedDateFrom.setText("No Date Selected");
      oSelectedDateTo.setText("No Date Selected");
      this._model.setProperty("/plotData", [])
      this._model.setProperty("/monitoringPoints", [])
    }
    this.updateMonitoringData()

  },

  updateMonitoringData: function () {
    if (this._model.getProperty("/dateFrom") && this._model.getProperty("/dateTo")) {
      MonitoringService.getMonitoringPoints(this._model.getProperty("/datasetId"));
    }
  },


  handleWeekNumberSelect: function(oEvent) {
    var oDateRange = oEvent.getParameter("weekDays");
    // var iWeekNumber = oEvent.getParameter("weekNumber");
    this._updateText(oDateRange);
  },

  _selectWeekInterval: function(iDays) {
    var oCurrent = new Date();     // get current date
    var iWeekstart = oCurrent.getDate() - oCurrent.getDay() + 1;
    var iWeekend = iWeekstart + iDays;       // end day is the first day + 6
    var oMonday = new Date(oCurrent.setDate(iWeekstart));
    var oSunday = new Date(oCurrent.setDate(iWeekend));

    var oCalendar = this.byId("calendar");

    oCalendar.removeAllSelectedDates();
    oCalendar.addSelectedDate(new DateRange({startDate: oMonday, endDate: oSunday}));

    this._updateText(oCalendar.getSelectedDates()[0]);
  },

  setDefaultDateInterval: function () {
    let oEnd = new Date()
    // Two weeks before today
    let oStart = new Date(oEnd.getTime() - 14 * 24 * 60 * 60 * 1000);

    let oCalendar = this.byId("calendar");
    oCalendar.removeAllSelectedDates();
    oCalendar.addSelectedDate(new sap.ui.unified.DateRange({startDate: oStart, endDate: oEnd}))
    this._updateText(oCalendar.getSelectedDates()[0]);
  }

});
