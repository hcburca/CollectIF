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

//adds various event listeners
$(document).ready(function () {
    //adds on 'click' listener to the submit button
    $('#loginForm').submit(function (ev) {
        ev.preventDefault();
        sendLogin();
    });
    //adds on 'click' listener to the submit button
    $('#signupForm').submit(function (ev) {
        ev.preventDefault();
        sendRegister();
    });
    
    //adds on 'hidden' listener to the login modal
    $('#login-modal').on('hidden.bs.modal', function () {
        $('#login-failed').removeClass("alert alert-danger");
        $('#login-failed').html("");
        document.getElementById("loginForm").reset();
    });
    
    //adds on 'hidden' listener to the register modal
    $('#signup-modal').on('hidden.bs.modal', function () {
        $('#signup-status').removeClass("alert alert-danger");
        $('#signup-status').html("");
        document.getElementById("signupForm").reset();
    });
});

// serves to send an ajax post request to server with login details 
function sendLogin() {
    $.ajax({
        beforeSend: function () {
            $('#login-modal').addClass('loading');
            $('#loginForm input').attr('disabled', true);
            $('#register-link').addClass("disabled");
            $('.btn-login').attr('disabled', true);
        },
        url: './LoginServletAdherent',
        dataType: 'json',
        type: 'POST',
        data: {
            action: 'seConnecter',
            email: $('#login-email').val()
        }
    })
            .done(function (data) {
                if (data.result == 'success') {
                    $(location).attr('href', data.redirect);
                } else if (data.result == 'failure') {
                    $('#login-failed').html("<p>" + data.message + "</p>");
                    $('#login-failed').addClass('alert alert-danger');
                } else {
                    $('#login-failed').html("<p> Problem inconnue, veuillez réessayer plus tard.</p>");
                    $('#login-failed').addClass('alert alert-danger');
                }
            })
            .fail(function () {
                $('#login-failed').html("<p> Problem de serveur, veuillez réessayer plus tard.</p>");
                $('#login-failed').addClass('alert alert-danger');
            })
            .always(function () {
                $('#login-modal').removeClass('loading');
                $('#loginForm input').attr('disabled', false);
                $('#register-link').removeClass("disabled");
                $('.btn-login').attr('disabled', false);
            });
}
// -----

// serves to send an ajax request for user registration 
function sendRegister() {
    $.ajax({
        beforeSend: function () {
            $('#signup-modal').addClass('loading');
            $('#singupForm input').attr('disabled', true);
            $('#login-link').addClass("disabled");
            $('.btn-signup').attr('disabled', true);
        },
        url: './LoginServletAdherent',
        dataType: 'json',
        type: 'POST',
        data: {
            action: 'sEnregistrer',
            familyname: $('#signup-fname').val(),
            firstname: $('#signup-gname').val(),
            email: $('#signup-email').val(),
            adresse: $('#signup-address').val()
        }
    })
            .done(function (data) {
                if (data.result == "success") {
                    document.getElementById('signupForm').reset();
                    $('#signup-status').html("<p>" + data.message + "</p>");
                    $('#signup-status').removeClass("alert-danger").addClass('alert alert-success');
                } else if (data.result == "failure") {
                    $('#signup-status').html("<p>" + data.message + "</p>");
                    $('#signup-status').removeClass("alert-success").addClass('alert alert-danger');
                } else {
                    $('#signup-status').html("<p> Probleme inconnue, veuillez réessayer plus tard.</p>");
                    $('#signup-status').removeClass("alert-success").addClass('alert alert-danger');
                }
            })
            .fail(function () {
                $('#signup-status').html("<p> Problem de serveur, veuillez réessayer plus tard.</p>");
                $('#signup-status').removeClass("alert-success").addClass('alert alert-danger');
            })
            .always(function () {
                $('#signup-modal').removeClass('loading');
                $('#registerForm input').attr('disabled', false);
                $('#login-link').removeClass('disabled');
                $('.btn-signup').attr('disabled', false);
            });
}
// -----

