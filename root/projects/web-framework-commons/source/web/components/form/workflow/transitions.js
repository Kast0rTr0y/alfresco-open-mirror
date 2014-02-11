/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
 
/**
 * Transitions form component.
 * 
 * @namespace Alfresco
 * @class Alfresco.Transitions
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * Transitions constructor.
    * 
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.Transitions} The new Transitions instance
    * @constructor
    */
   Alfresco.Transitions = function(htmlId)
   {
      Alfresco.Transitions.superclass.constructor.call(this, "Alfresco.Transitions", htmlId, ["button", "container"]);
      YAHOO.Bubbling.on("taskDetailedData", this.onTaskDetailedData, this);
      
      return this;
   };
   
   YAHOO.extend(Alfresco.Transitions, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * The current value, it's a list of comma separated
          * transitions in the format transition_id|label
          * 
          * @property currentValue
          * @type string
          */
         currentValue: "",
         
         /**
          * List of transition objects representing
          * the transitions for the task.
          * 
          * @property transitions
          * @type array
          */
         transitions: null
      },
      
      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function Transitions_onReady()
      {
         // setup the transitions array
         this._processTransitions();
         
         // generate buttons for each transition
         this._generateTransitionButtons();
      },
      
      /**
       * Event handler called when a transition button is clicked.
       *
       * @method onClick
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onClick: function Transitions_onClick(e, p_obj)
      {
         //MNT-2196 fix, disable transition button to prevent multiple execution
         p_obj.set("disabled", true);
         // determine what button was pressed by it's id
         var buttonId = p_obj.get("id");
         var transitionId = buttonId.substring(this.id.length+1);
         
         // get the hidden field
         var hiddenField = this._getHiddenField();

         // set the hidden field value
         Dom.setAttribute(hiddenField, "value", transitionId);
         
         if (Alfresco.logger.isDebugEnabled())
            Alfresco.logger.debug("Set transitions hidden field to: " + transitionId);
         
         // attempt to submit the form
         Alfresco.util.submitForm(p_obj.getForm());
      },
      
      /**
       * Processes the encoded transitions string into.
       * 
       * @method _processTransitions
       * @private
       */
      _processTransitions: function Transitions__processTransitions()
      {
         // process the current value and create the list of transitions
         this.options.transitions = [];
         
         if (Alfresco.logger.isDebugEnabled())
            Alfresco.logger.debug("Processing transitions for field '" + this.id + "': " + this.options.currentValue);
         
         // process the transitions
         if (this.options.currentValue !== null && this.options.currentValue.length > 0)
         {
            var transitionPairs = this.options.currentValue.split(",");
            for (var i = 0, ii = transitionPairs.length; i < ii; i++)
            {
               // retrieve the transition info and split
               var transitionInfo = transitionPairs[i].split("|");
               
               // add the transition as an object
               this.options.transitions.push(
               {
                  id: transitionInfo[0],
                  label: transitionInfo[1]
               });
            }
         }
         
         if (Alfresco.logger.isDebugEnabled())
            Alfresco.logger.debug("Built transitions list: " + YAHOO.lang.dump(this.options.transitions));
      },
      
      /**
       * Generates a YUI button for each transition.
       * 
       * @method _generateTransitionButtons
       * @private
       */
      _generateTransitionButtons: function Transitions__generateTransitionButtons()
      {
         // create a submit button for each transition
         for (var i = 0, ii = this.options.transitions.length; i < ii; i++)
         {
            this._generateTransitionButton(this.options.transitions[i]);
         }
      },
      
      /**
       * Generates a YUI button for the given transition.
       * 
       * @method _generateTransitionButton
       * @param transition {object} An object representing the transition
       * @private
       */
      _generateTransitionButton: function Transitions__generateTransitionButton(transition)
      {
         // create a button and add to the DOM
         var button = document.createElement('input');
         button.setAttribute("id", this.id + "-" + transition.id);
         button.setAttribute("value", transition.label);
         button.setAttribute("type", "button");
         Dom.get(this.id + "-buttons").appendChild(button);
         
         // create the YUI button and register the event handler
         var button = Alfresco.util.createYUIButton(this, transition.id, this.onClick);
         
         if (transition.id == "accept")
         {
            this.widgets.accept = button;
         }
         
         // register the button as a submitElement with the forms runtime instance
         YAHOO.Bubbling.fire("addSubmitElement", button);
      },
      
      /**
       * Retrieves, creating if necessary, the hidden field used
       * to hold the selected transition.
       * 
       * @method _getHiddenField
       * @return The hidden field element
       * @private
       */
      _getHiddenField: function Transitions__getHiddenField()
      {
         // create the hidden field (if necessary)
         var hiddenField = Dom.get(this.id + "-hidden");
         if (hiddenField === null)
         {
            hiddenField = document.createElement('input');
            hiddenField.setAttribute("id", this.id + "-hidden");
            hiddenField.setAttribute("type", "hidden");
            hiddenField.setAttribute("name", "prop_transitions");
            
            Dom.get(this.id).appendChild(hiddenField);
         }
         
         return hiddenField;
      },
      
       /**
       * Event handler called when the "taskDetailedData" event is received
       *
       * @method: onTaskDetailedData
       */
      onTaskDetailedData: function ActivitiTransitions__gonTaskDetailedData(layer, args)
      {
         var task = args[1];
         
         if (task.name && task.name == "inwf:invitePendingTask" && task.properties.inwf_resourceName)
         {
            this.siteName = task.properties.inwf_resourceName;
            var url = Alfresco.constants.PROXY_URI + "api/sites/" + this.siteName;
            
            Alfresco.util.Ajax.request(
            {
               method: Alfresco.util.Ajax.GET,
               url: url,
               failureCallback:
               {
                  fn: this.onFailure,
                  scope: this
               }
            });
         }
      },
      
       /**
       * Failure handler
       *
       * @method onFailure
       * @param response {object} The response from the ajax request
       */
      onFailure: function ReplicationJob_onFailure()
      {
         if (this.widgets.accept)
         {
            this.widgets.accept.set("disabled", true);
         }
         
         Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("form.popup.site.absent", this.siteName),
            displayTime: 2
         });
      }
   });
})();