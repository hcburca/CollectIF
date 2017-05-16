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
//checks the pages of the demands carousel to hide or show navigation buttons
//depeding on the number of slides and the current slide
var checkitem = function () {
    var $this;
    $this = $("#myCarousel");
    if ($("#myCarousel .carousel-inner .item").length === 1) {
        $this.children(".carousel-control").hide();
    } else if ($("#myCarousel .carousel-inner .item:first").hasClass("active")) {
        $this.children(".left").hide();
        $this.children(".right").show();
    } else if ($("#myCarousel .carousel-inner .item:last").hasClass("active")) {
        $this.children(".right").hide();
        $this.children(".left").show();
    } else {
        $this.children(".carousel-control").show();
    }
};
// -----

$(document).ready(function () {
    $("#myCarousel").on("slid.bs.carousel", "", checkitem);

    //highlight navigation menu selection
    // get the full URL at the address bar
    var url = window.location.href;
    // get the 'a' tags of the menu
    $("#nav-menu a").each(function () {
        // checks if its the same on the address bar
        if (url == (this.href)) {
            $(this).closest("li").addClass("active");
        }
    });
});


//orders demands and events based on date and moment
function orderDemands(arrayOfDemands) {
    arrayOfDemands.sort(function (dem1, dem2) {
        var d1 = new Date(dem1.date);
        var d2 = new Date(dem2.date);
        if ((d1 - d2) === 0) {
            return dem1.momentOrder - dem2.momentOrder;
        }
        return d1 - d2;
    });
}
// -----

//sends an ajax request to get the user info including the current demands and events 
$(function () {
    var list = document.getElementById("myCarousel-inner");
    $.ajax({
        url: './ActionServletAdherent',
        type: 'POST',
        data: {
            action: 'listerInfos'
        },
        dataType: 'json',
        cached: false
    })
            .done(function (data) {
                if (data.result != null && data.result == "failure") {
                    var htmlServerProb = [
                        '<div class="item active alert alert-danger text-justify">',
                        '<p>' + data.message + '</p>',
                        '</div>'
                    ].join("\n");
                    $(list).append(htmlServerProb);
                } else {
                    //show username
                    var username = data.adherent.name;
                    $("#name-user").html(username);
                    
                    //show demands in process
                    var demandes = data.demandes;
                    // if there is no demand or event in progress
                    if (demandes.length === 0) {
                        var htmlNoDemand = [
                            '<div class="item active text-left">',
                            '<div class="flex-row">',
                            '<i class="fa fa-ban"></i>',
                            '<p> Aucune demande en attente</p>',
                            '</div>',
                            '<p class="alert alert-info text-justify">',
                            'Veuillez naviguer dans <strong>Faire une demande</strong> pour faire une nouvelle demande.',
                            '</p>',
                            '</div>'
                        ].join("\n");
                        $(list).append(htmlNoDemand);
                    } else {
                        orderDemands(demandes);
                        for (var i = 0; i < demandes.length; i++) {
                            var itemClasses = "item text-left";
                            if (i === 0) {
                                itemClasses += " active";
                            }
                            var htmlItem = [
                                '<div class="' + itemClasses + '">',
                                '<h4 class="text-center">' + demandes[i].denomination + '</h4>',
                                '<div class="flex-row">',
                                '<i class="fa fa-exclamation-circle"></i>',
                                '<p>' + demandes[i].status + '</p>',
                                '</div>'
                            ];
                            if (demandes[i].participants != null) {
                                var participants = demandes[i].participants;
                                var participantsNames = [];
                                participants.forEach(function (part) {
                                    participantsNames.push(part.name);
                                });
                                var participantsString = participantsNames.join("</br>");
                                var htmlParticipants = [
                                    '<div class="flex-row">',
                                    '<i class="fa fa-users"></i>',
                                    '<p title="Participants" class="paragraph-popover" data-toggle="popover" data-placement="top" data-html="true" data-content="' + participantsString + '">Cliquer pour les participants</p>',
                                    '</div>'
                                ];
                                htmlItem = htmlItem.concat(htmlParticipants);
                            }
                            // place, if any
                            if (demandes[i].place != null) {
                                var htmlPlace = [
                                    '<div class="flex-row">',
                                    '<i class="fa fa-map-marker"></i>',
                                    '<p>' + demandes[i].place + '</p>',
                                    '</div>'
                                ];
                                htmlItem = htmlItem.concat(htmlPlace);
                            }
                            //date
                            var htmlDate = [
                                '<div class="flex-row">',
                                '<i class="fa fa-clock-o"></i>',
                                '<p>' + demandes[i].date + '</p>',
                                '</div>',
                                '</div>'
                            ];

                            htmlItem = htmlItem.concat(htmlDate);
                            $(list).append(htmlItem.join("\n"));
                        }
                    }
                }

            })
            .fail(function () {
                var htmlFailure = [
                    '<div class="item active alert alert-danger text-justify">',
                    '<p>Problem de serveur, veuillez réessayer plus tard.</p>',
                    '</div>'
                ].join("\n");
                $(list).append(htmlFailure);
            })
            .always(function () {
                checkitem();
                $('[data-toggle="popover"]').popover({
                    container: 'body'
                });
            })
});
// -----

