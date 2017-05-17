/* 
 * Copyright (C) 2017 Horia
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
var activite;               //the choice of activity
var dayMoment;              //the choice of day period
var date;                   //the choice of date
var page = 0;               //a counter for the number of 'pages' of days to display (10 days per page)
var activites = [];         //stores the json activities
var lastDate;               //a variable to the last date which is currently displayed on the page
var datesToGenerate = 10;   //number of dates to generate on page load, and then on each click on 'load more'
var moments = ["matin", "après-midi", "soir"];

//gets the json activity by its id
function getActivityById(id) {
    for (var i = 0; i < activites.length; ++i) {
        if (activites[i].id == id) {
            return activites[i];
        }
    }
}
// -----

//gets the value of the url parameter passed to the function, if present in the url
var getUrlParameter = function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;
    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
};
// -----

//sends ajax request to server to get the list of activities and adds them to the 'select' element
$(function () {
    $.ajax({
        url: './ActionServletAdherent',
        type: 'POST',
        data: {
            action: 'listerActivites',
        },
        dataType: 'json'
    })
            .done(function (data) {
                if (data.result != null && data.result == "failure") {
                    var htmlFailure = [
                        '<div class="alert alert-danger">',
                        '<p>' + data.message + '</p>',
                        '</div>'
                    ].join("\n");
                    $("#activity-payed").after(htmlFailure);
                } else {
                    //show activities
                    var list = document.getElementById("activity-choice");
                    activites = data.activites;
                    if (activites.length === 0) {
                        var option = document.createElement('option');
                        option.selected = true;
                        option.disabled = true;
                        option.appendChild(document.createTextNode('Aucune activité disponible pour le moment'));
                        list.appendChild(option);
                    } else {
                        var i;
                        for (i = 0; i < activites.length; i++) {
                            var option = document.createElement('option');
                            option.value = activites[i].id;
                            option.appendChild(document.createTextNode(activites[i].denomination));
                            list.appendChild(option);
                        }
                        
                        //sort the list of activities
                        var options = $("#activity-choice option:not(:first)");
                        options.sort(function(a,b) {
                           if (a.text > b.text) return 1;
                           else if (a.text < b.text) return -1;
                           else return 0;
                        });
                        $("#activity-choice option:not(:first)").remove();
                        $("#activity-choice").append(options);
                        
                        //select the right activity if there is a 'idActivite' parameter in the link
                        var idActivite = getUrlParameter("idActivite");
                        if (idActivite) {
                            $("#activity-choice").val(idActivite).trigger("change");
                        }
                    }
                }
            })
            .fail(function () {
                var htmlFail = [
                    '<div class="alert alert-danger">',
                    '<p>Le serveur n\'a pas pu charger les activités.</p>',
                    '<p>Veuillez réessayer plus tard.</p>',
                    '</div>'
                ].join("\n");
                $("#activity-payed").after(htmlFail);
            })
});
// -----

//formats the date in french format
function formatDate(date) {
    var monthNames = [
        "Janvier", "Février", "Mars",
        "Avril", "Mai", "Juin", "Juillete",
        "Août", "Septembre", "Octobre",
        "Novembre", "Décembre"
    ];
    var dayNames = [
        "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"
    ];
    date.setHours(0, 0, 0, 0);
    var now = new Date();
    now.setHours(0, 0, 0, 0);
    var tomorrow = new Date(now.getTime() + 86400000);
    if (date.getTime() == now.getTime()) {
        return "Aujourd'hui";
    } else if (date.getTime() == tomorrow.getTime()) {
        return "Demain";
    } else {
        var day = date.getDate();
        var dayIndex = date.getDay();
        var monthIndex = date.getMonth();
        var year = date.getFullYear();
        return dayNames[dayIndex] + ", " + day + ' ' + monthNames[monthIndex] + ' ' + year;
    }
}
// -----

//reverse formats the date from french format to dd/MM/yyyy
function shortFormatDate(date) {
    var monthNames = [
        "Janvier", "Février", "Mars",
        "Avril", "Mai", "Juin", "Juillete",
        "Août", "Septembre", "Octobre",
        "Novembre", "Décembre"
    ];
    var now = new Date();
    switch (date) {
        case "Aujourd'hui" :
            return now.getDate() + '/' + (now.getMonth() + 1) + '/' + now.getFullYear();
        case "Demain"      :
            now.setDate(now.getDate() + 1);
            return now.getDate() + '/' + (now.getMonth() + 1) + '/' + now.getFullYear();
        default            :
            date = date.substring(date.indexOf(' ') + 1, date.length);
            var day = date.substring(0, date.indexOf(' '));
            date = date.substring(date.indexOf(' ') + 1, date.length);
            var month = monthNames.indexOf(date.substring(0, date.indexOf(' '))) + 1;
            var year = date.substring(date.indexOf(' ') + 1, date.length);
            return day + '/' + month + '/' + year;
    }
}
// -----

//updates the different elements of the page depending on how the demand has advanced
function updatePage() {
    var status = document.getElementById('demande-status');
    var validerButton = document.getElementById('demande-validate');
    if (activite == null && dayMoment == null) {
        $("#demande-status").removeClass("alert-success alert-danger").addClass("alert-warning");
        validerButton.disabled = true;
        //status.innerHTML = 'Veuillez choisir une activité';
    } else if (dayMoment == null) {
        $("#demande-status").removeClass("alert-success alert-danger").addClass("alert-warning");
        validerButton.disabled = true;
        status.innerHTML = 'Veuillez choisir une date et un moment de la journée, et en fin validez.';
    } else {
        $("#demande-status").removeClass("alert-warning alert-danger").addClass("alert-success");
        status.innerHTML = "Vous pouvez maintenant valider votre demande pour: " + activite.denomination.toLowerCase() + ", " + date.toLowerCase() + " " + dayMoment + ".";
        validerButton.disabled = false;
    }
}
// -----

//generates 'nrDays' days starting with the 'startingDate'
function generateDates(startingDate, nrDays) {
    var htmlDates = [];
    var classInactive = "";
    if (activite == null) {
        classInactive = "btn-inactive ";
    }
    for (var i = 0; i < nrDays; i++) {
        var htmlDate = [
            '<div class="row row-flex row-margin">',
            '<div class="col-md-5">',
            '<h4 class="date">' + formatDate(startingDate) + '</h4>',
            '</div>',
            '<div class="col-md-7">',
            '<div class="btn-group btn-group-justified">',
            '<div class="btn-group">',
            '<button type="button" class="btn btn-primary ' + classInactive + 'moment" data-trigger="hover" data-html="true" data-toggle="tooltip" title="">matin</button>',
            '</div>',
            '<div class="btn-group">',
            '<button type="button" class="btn btn-primary ' + classInactive + 'moment" data-trigger="hover" data-html="true" data-toggle="tooltip" title="">après-midi</button>',
            '</div>',
            '<div class="btn-group">',
            '<button type="button" class="btn btn-primary ' + classInactive + 'moment" data-trigger="hover" data-html="true" data-toggle="tooltip" title="">soir</button>',
            '</div>',
            '</div>',
            '</div>',
            '</div>',
        ];
        htmlDates = htmlDates.concat(htmlDate);
        startingDate.setDate(startingDate.getDate() + 1);
    }
    $("#all-moments .row:last").before(htmlDates.join("\n"));
}
// -----

//sends an ajax request to get the participants for each moment
function sendRequestForParticipants(idActivite) {
    var nrDays = $(".date").length;
    $(function () {
        $.ajax({
            beforeSend: function () {
                document.getElementById("activity-choice").disabled = true;
                $('body').addClass('loading');
            },
            url: './ActionServletAdherent',
            type: 'POST',
            data: {
                action: 'listerUneActivite',
                id: idActivite,
                nrDays: nrDays
            },
            dataType: 'json'
        })
                .done(function (data) {
                    if (data.result != null && data.result == "failure") {
                        var htmlFailure = [
                            '<div id="errorSendRequestParticipants" class="alert alert-warning">',
                            '<p>' + data.message + '</p>',
                            '</div>'
                        ].join("\n");
                        $("#activity-payed").after(htmlFailure);
                    } else {
                        var daysData = data.activite.participantsParDate;
                        var moments = $(".moment");
                        for (var i = 0; i < daysData.length; i++) {
                            if (daysData[i].nr > 0 && daysData[i].nr < data.activite.nbParticipants) {
                                $(moments[i]).find("span").remove();
                                $(moments[i]).append("<span> (" + daysData[i].nr + "/" + data.activite.nbParticipants + ")</span>");
                            }
                            //if the user is busy for a particular date and moment then show him that 
                            if (daysData[i].statusMoment !== "Ce moment et cette date sont disponibles.") {
                                $(moments[i]).addClass("btn-inactive");
                                $(moments[i]).attr('data-original-title', daysData[i].statusMoment);
                            } else {
                                //display the participants on hover if there are any
                                if (daysData[i].nr > 0 && daysData[i].nr < data.activite.nbParticipants) {
                                    var participantsTable = ["Participants:"];
                                    for (var j = 0; j < daysData[i].nr; j++) {
                                        participantsTable.push(daysData[i].participants[j].nom);
                                    }
                                    $(moments[i]).attr('data-original-title', participantsTable.join("</br>"));
                                }
                            }
                        }
                    }
                })
                .fail(function () {
                    var htmlFail = [
                        '<div id="errorSendRequestParticipants" class="alert alert-warning">',
                        '<p>Le serveur n\'a pas pu charger les informations liées aux demandes déjà existantes sur: ' + activite.denomination + '.</p>',
                        '<p>Vous pouvez continuer à l\'aveuglette ou réessayer plus tard.</p>',
                        '</div>'
                    ].join("\n");
                    $("#activity-payed").after(htmlFail);
                })
                .always(function () {
                    document.getElementById("activity-choice").disabled = false;
                    $('body').removeClass('loading');
                    $('[data-toggle="tooltip"]').tooltip({
                        'placement': 'bottom'
                    });
                });
    });
}
// -----

//sends an ajax request to register a demand with the selected activity, date and moment
function sendRequestRegisterDemand(idActivite, dateShortFormat, indexMoment) {
    $(function () {
        $.ajax({
            beforeSend: function () {
                document.getElementById("demande-validate").disabled = true;
                $('body').addClass('loading');
            },
            url: './ActionServletAdherent',
            type: 'POST',
            data: {
                action: 'enregistrerDemande',
                idActivite: idActivite,
                date: dateShortFormat,
                moment: indexMoment

            },
            dataType: 'json'
        })
                .done(function (data) {
                    if (data.result != null && data.result == "failure") {
                        $("#demande-status").removeClass("alert-warning alert-success").addClass("alert-danger");
                        $("#demande-status").text(data.message);
                    } else if (data.result != null && data.result == "success") {
                        //appends a modal popup to the body and shows it
                        var htmlPopup = [
                            '<div id="modal-success" class="modal fade">',
                            '<div class="modal-dialog" role="document">',
                            '<div class="modal-content">',
                            '<div class="modal-header">',
                            '<h5 class="modal-title">Succès!</h5>',
                            '<button type="button" class="close" data-dismiss="modal" aria-label="Close">',
                            '<span aria-hidden="true">&times;</span>',
                            '</button>',
                            '</div>',
                            '<div class="modal-body">',
                            '<div class="alert alert-success">',
                            '<p>' + data.message + '</p>',
                            '</div>',
                            '</div>',
                            '<div class="modal-footer">',
                            '<button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>',
                            '</div>',
                            '</div>',
                            '</div>',
                            '</div>'
                        ].join("\n");
                        $('body').append(htmlPopup);
                        $('#modal-success').modal({show: false});
                        $('#modal-success').modal('show');
                        //adds an event listener to the modal to redirect the page to the url indicated by the 
                        //server on closure of the modal
                        $('#modal-success').on('hidden.bs.modal', function () {
                            $(location).attr('href', data.redirect);
                        });
                    } else {
                        $("#demande-status").removeClass("alert-warning alert-success").addClass("alert-danger");
                        $("#demande-status").text("Problem inconnue, veuillez réessayer plus tard.");
                    }
                })
                .fail(function () {
                    $("#demande-status").removeClass("alert-warning alert-success").addClass("alert-danger");
                    $("#demande-status").text("Problem de serveur, veuillez réessayer plus tard.");
                })
                .always(function () {
                    document.getElementById("demande-validate").disabled = false;
                    $('body').removeClass('loading');
                });
    });
}
// -----

//sets on 'load' actions for the page loading
window.addEventListener('load', function () {
    lastDate = new Date();
    generateDates(lastDate, datesToGenerate);

    //remove moments that have passed
    var now = new Date();
    var hours = now.getHours();
    if (hours >= 12) {
        $(".moment").first().remove();
        if (hours >= 18) {
            $(".moment").first().remove();
        }
    }
});

$(document).ready(function () {
//sets on 'change' actions for the activity selection
    $("#activity-choice").on('change', function () {
        //remove any changes to the moment menu
        dayMoment = null;
        $(".moment").removeClass("btn-inactive");
        $(".moment").attr("data-original-title", "")
        $(".moment").find("span").remove();
        $("#activeMoment").removeAttr('id');
        //remove any errors if they exist
        $("#errorSendRequestParticipants").remove();

        //send request for participants
        activite = getActivityById($(this).val());
        sendRequestForParticipants(activite.id);
        //change payed/free information
        $("#activity-payed p").first().text(" " + activite.payed);
        //change image
        $("#activity-img").attr('src', activite.imgUrl);
        $("#activity-img").attr('alt', activite.denomination);
        $("#activity-img").attr('title', activite.denomination);
        //change participants information
        //add number of participants required
        var htmlNbParticipants;
        if ($("#activity-nbpart").length) {
            htmlNbParticipants = [
                '<i class="fa fa-list-alt"></i>',
                '<p>Nombre de participants requis: ' + activite.nbParticipants + '<p>'
            ].join("\n");
            $("#activity-nbpart").html(htmlNbParticipants);
        } else {
            htmlNbParticipants = [
                '<div id="activity-nbpart" class="flex-row">',
                '<i class="fa fa-list-alt"></i>',
                '<p>Nombre de participants requis: ' + activite.nbParticipants + '<p>',
                '</div>'
            ].join("\n");
            $("#activity-payed").after(htmlNbParticipants);
        }
        updatePage();
    });

    //sets on 'click' actions for the moment and date selection
    $("#all-moments").on('click', '.moment', function (e) {
        if ($(this).hasClass('btn-inactive')) {
            e.preventDefault();
        } else {
            dayMoment = $(this).contents().filter(function () {
                return this.nodeType == 3;
            }).first().text();
            var currentActive = $('#activeMoment');
            if (currentActive.length !== 0) {
                $(currentActive).removeAttr('id');
            }
            $(this).attr("id", "activeMoment");
            //save the date
            date = $(this).parents(".row").first().find(".date").text();
            updatePage();
        }
    });

    //sets on 'click' actions for the load more button
    $("#moments-load-more").on('click', function () {
        generateDates(lastDate, datesToGenerate);
        if (activite != null) {
            sendRequestForParticipants(activite.id);
        }
    });

    //sets on 'click' actions for the validate button
    $("#demande-validate").on('click', function () {
        sendRequestRegisterDemand(activite.id, shortFormatDate(date), moments.indexOf(dayMoment));
    });
});


